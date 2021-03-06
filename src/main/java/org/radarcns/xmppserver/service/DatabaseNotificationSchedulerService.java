package org.radarcns.xmppserver.service;

import com.github.kagkarlsson.scheduler.Scheduler;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import com.github.kagkarlsson.scheduler.task.helper.OneTimeTask;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.dbcp.BasicDataSource;
import org.radarcns.xmppserver.ccs.CcsClientWrapper;
import org.radarcns.xmppserver.commandline.CommandLineArgs;
import org.radarcns.xmppserver.database.DataSourceWrapper;
import org.radarcns.xmppserver.database.DatabaseCleanupTask;
import org.radarcns.xmppserver.database.DefaultDatabaseCleanupTask;
import org.radarcns.xmppserver.database.NotificationDatabaseHelper;
import org.radarcns.xmppserver.model.Data;
import org.radarcns.xmppserver.model.Notification;
import org.radarcns.xmppserver.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database notification scheduler service class for the XMPP Server This has functionality for both
 * in-memory, persisent and server data bases. Abstract class so has to instantiated through
 * sub-classes. Look at {@link InMemoryDatabaseNotificationSchedulerService}, {@link
 * PersistentDatabaseNotificationSchedulerService} and {@link
 * ServerDatabaseNotificationSchedulerService}
 *
 * @author yatharthranjan
 */
public abstract class DatabaseNotificationSchedulerService implements NotificationSchedulerService {

  private static final String TASK_NAME = "notification-one-time";
  private static final String TASK_ID_DELIMITER = "+";
  private static final Logger logger =
      LoggerFactory.getLogger(DatabaseNotificationSchedulerService.class);
  private final DataSourceWrapper scheduleDataSourceWrapper;
  private final NotificationDatabaseHelper databaseHelper;
  private final DatabaseCleanupTask databaseCleanupTask;
  private Scheduler scheduler;
  private OneTimeTask<Notification> notificationOneTimeTask;
  private boolean isRunning = false;
  private Map<User, Set<Notification>> cacheNotifications;

  DatabaseNotificationSchedulerService(String type) {
    BasicDataSource basicDataSource = new BasicDataSource();
    basicDataSource.setDriverClassName("org.hsqldb.jdbcDriver");
    basicDataSource.setUrl("jdbc:hsqldb:" + type + ":" + CommandLineArgs.dbPath);
    basicDataSource.setUsername(CommandLineArgs.dbUser);
    basicDataSource.setPassword(CommandLineArgs.dbPass);
    this.scheduleDataSourceWrapper = new DataSourceWrapper(basicDataSource);

    BasicDataSource dataSource = new BasicDataSource();
    dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
    dataSource.setUrl(
        "jdbc:hsqldb:"
            + type
            + ":"
            + CommandLineArgs.dbPath.substring(0, CommandLineArgs.dbPath.lastIndexOf('/'))
            + "/status");
    dataSource.setUsername(CommandLineArgs.dbUser);
    dataSource.setPassword(CommandLineArgs.dbPass);
    this.databaseHelper = new NotificationDatabaseHelper(dataSource);
    this.cacheNotifications = Collections.synchronizedMap(new HashMap<>());

    long nightTime3am =
        LocalDateTime.now()
            .until(
                LocalDate.now().plusDays(1).atStartOfDay().plus(Duration.ofHours(3)),
                ChronoUnit.MINUTES);
    // Clean the DB every day looking for delivered notifications older than 30 days
    databaseCleanupTask =
        new DefaultDatabaseCleanupTask(
            nightTime3am,
            TimeUnit.DAYS,
            CommandLineArgs.notificationCleanUpinterval,
            TimeUnit.DAYS,
            CommandLineArgs.notificationExpiry,
            new DatabaseCleanupService(databaseHelper));
  }

  @Override
  public void start() {

    if (!isRunning) {
      scheduleDataSourceWrapper.createTableForScheduler();
      databaseHelper.createTableForStatus();
      databaseHelper.createTableforNotification();

      notificationOneTimeTask =
          Tasks.oneTime(TASK_NAME, Notification.class)
              .execute(
                  (inst, ctx) -> {
                    Notification notification = inst.getData();
                    if (notification
                        .getScheduledTime()
                        .toInstant()
                        .plus(Duration.ofSeconds(notification.getTtlSeconds()))
                        .isAfter(Instant.now())) {
                      long ttlSecondsNow =
                          notification.getTtlSeconds()
                              - (Instant.now().getEpochSecond()
                                  - notification.getScheduledTime().toInstant().getEpochSecond());
                      Notification newNotification =
                          new Notification.Builder(notification)
                              .setTtlSeconds((int) ttlSecondsNow)
                              .build();
                      CcsClientWrapper.getInstance().sendNotification(newNotification);
                    } else {
                      logger.warn(
                          "The Notification {} delivery Time window has elapsed. Skipping it...",
                          notification);
                    }
                  });

      scheduler =
          Scheduler.create(scheduleDataSourceWrapper.getDataSource(), notificationOneTimeTask)
              .threads(5)
              .build();

      scheduler.start();

      databaseCleanupTask.startCleanup();

      Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
      migrateDataFromScheduler();

      isRunning = true;

    } else {
      logger.warn(
          "Cannot start an instance of {} when it is already running.", this.getClass().getName());
    }
  }

  /**
   * Migrates data from the Scheduler database({@link CommandLineArgs#dbPath}) to the new Status
   * database (/usr/hsql/status). This is required to make sure that the two are synchronised and
   * provide backward compatibility.
   */
  private void migrateDataFromScheduler() {
    scheduler.getScheduledExecutions(
        taskScheduledExecution -> {
          String[] ids =
              taskScheduledExecution
                  .getTaskInstance()
                  .getId()
                  .split(Pattern.quote(TASK_ID_DELIMITER));
          Notification notification = (Notification) taskScheduledExecution.getData();
          if (!databaseHelper.checkIfNotificationExists(notification)) {
            try {
              databaseHelper.addNotification(
                  notification, String.valueOf(notification.hashCode()), ids[2]);
              logger.info("Notification added from scheduler database");
            } catch (Exception e) {
              logger.error(
                  "Cannot insert from scheduler database notification {} due to {}",
                  notification,
                  e.fillInStackTrace());
            }
          }
        });
  }

  @Override
  public void stop() {

    if (isRunning) {
      isRunning = false;
      databaseCleanupTask.stopCleanup();
      scheduler.stop();
      scheduleDataSourceWrapper.close();
    } else {
      logger.warn(
          "Cannot stop an instance of {} when it is not running.", this.getClass().getName());
    }
  }

  /**
   * Schedules a {@link Collection} of {@link Data} using the database. The reduces the number of
   * database transactions to depend on the number of {@link User} instead number of {@link
   * Notification}. This is because at a given burst of schedule requests, most of them are going to
   * be from a limited number of users, thus reducing the number of database transactions and
   * improving performance.
   *
   * @param data {@link Collection} of {@link Data} to be scheduled.
   */
  @Override
  public synchronized void schedule(Collection<Data> data) {

    Set<Notification> notifications = new HashSet<>();
    try {
      notifications =
          data.stream()
              .map(s -> Notification.getNotification(s.getFrom(), s.getPayload()))
              .filter(Objects::nonNull)
              .collect(Collectors.toSet());
    } catch (RuntimeException exc) {
      logger.error("Error while processing notification data: ", exc);
    }

    Set<User> users =
        notifications.stream()
            .map(n -> new User(n.getSubjectId(), n.getRecepient()))
            .collect(Collectors.toSet());

    // TODO: Insert Notifications in batches using batchUpdate()

    for (User user : users) {
      Set<Notification> notificationSet =
          Collections.synchronizedSet(
              new HashSet<>(
                  databaseHelper.findNotifications(user.getSubjectId(), user.getFcmToken())));
      cacheNotifications.put(user, notificationSet);
    }
    notifications.forEach(this::scheduleUsingCache);
  }

  private void addNotification(Notification notification) {
    String taskUuid = UUID.randomUUID().toString();
    // Task id consists of 3 parts -- The FCM token, the subjectID (or any other custom ID)
    // and a UUID to make the task unique
    String taskId =
        notification.getRecepient()
            + TASK_ID_DELIMITER
            + notification.getSubjectId()
            + TASK_ID_DELIMITER
            + taskUuid;
    TaskInstance<?> taskInstance = notificationOneTimeTask.instance(taskId, notification);
    try {
      scheduler.schedule(taskInstance, notification.getScheduledTime().toInstant());
      logger.info("Task scheduled for notification {}", notification);
      databaseHelper.addNotification(
          notification, String.valueOf(notification.hashCode()), taskUuid);
    } catch (Exception e) {
      logger.error("Cannot insert notification {} due to {}", notification, e.fillInStackTrace());
      scheduler.cancel(taskInstance);
    }
  }

  private synchronized void scheduleUsingCache(Notification notification) {
    User user = new User(notification.getSubjectId(), notification.getRecepient());

    if (cacheNotifications.containsKey(user)
        && !cacheNotifications.get(user).contains(notification)) {
      cacheNotifications.get(user).add(notification);
      addNotification(notification);
    } else {
      logger.debug(
          "Notification already exists in cache for subject {} : {}",
          notification.getSubjectId(),
          notification);
    }
  }

  /**
   * Schedules a single notification. This checks if a notification already exists for a particular
   * subject and FCM token, if it does not exist then schedules a task. The messageId for FCM is
   * generate using Hashcode from the {@link Notification} object as each notification is supposed
   * to be unique.
   *
   * @param data data object encapsulating the scheduling information
   */
  @Override
  public synchronized void schedule(Data data) {
    if (isRunning) {
      Notification notification = Notification.getNotification(data.getFrom(), data.getPayload());
      if (notification != null) {
        if (!databaseHelper.checkIfNotificationExists(notification)) {
          addNotification(notification);
        } else {
          logger.debug(
              "Notification already exists for subject {} : {}",
              notification.getSubjectId(),
              notification);
        }
      }
    } else {
      logger.warn(
          "Cannot schedule using an instance of {} when it is not running. Please start the service first.",
          this.getClass().getName());
    }
  }

  @Override
  public synchronized void cancelUsingFcmToken(String from) {
    if (isRunning) {
      scheduler.getScheduledExecutions(
          taskScheduledExecution -> {
            String[] ids =
                taskScheduledExecution
                    .getTaskInstance()
                    .getId()
                    .split(Pattern.quote(TASK_ID_DELIMITER));
            if (ids.length == 3 && from.equals(ids[0])) {
              logger.info(
                  "Removing the scheduled task using token {} from thread {} - {}",
                  ids[0],
                  Thread.currentThread().getName(),
                  Thread.currentThread().getId());
              scheduler.cancel(taskScheduledExecution.getTaskInstance());
            }
          });
      databaseHelper.removeAllUndeliveredNotifications(null, from);
    } else {
      logger.warn(
          "Cannot cancelUsingFcmToken using an instance of {} when it is not running. Please start the service first.",
          this.getClass().getName());
    }
  }

  @Override
  public synchronized void cancelUsingCustomId(String id) {
    if (isRunning) {
      scheduler.getScheduledExecutions(
          taskScheduledExecution -> {
            String[] ids =
                taskScheduledExecution
                    .getTaskInstance()
                    .getId()
                    .split(Pattern.quote(TASK_ID_DELIMITER));
            if (ids.length == 3 && id.equals(ids[1])) {
              logger.info(
                  "Removing the scheduled task using id {} from thread {} - {}",
                  id,
                  Thread.currentThread().getName(),
                  Thread.currentThread().getId());
              scheduler.cancel(taskScheduledExecution.getTaskInstance());
            }
          });
      databaseHelper.removeAllUndeliveredNotifications(id, null);
    } else {
      logger.warn(
          "Cannot cancelUsingFcmToken using an instance of {} when it is not running. Please start the service first.",
          this.getClass().getName());
    }
  }

  /** Deprecated function -- Should not be used anymore. */
  @Deprecated
  @Override
  public synchronized void updateToken(String oldToken, String newToken) {
    if (isRunning) {
      // TODO update token based on subject id
      scheduler.getScheduledExecutions(
          taskScheduledExecution -> {
            if (taskScheduledExecution.getTaskInstance().getId().contains(oldToken)) {
              logger.info(
                  "Updating token on the scheduled task with id {} from thread {} - {}",
                  taskScheduledExecution.getTaskInstance().getId(),
                  Thread.currentThread().getName(),
                  Thread.currentThread().getId());
              Notification notification = (Notification) (taskScheduledExecution.getData());
              if (notification.getRecepient().equals(oldToken)) {
                // notification.setRecepient(newToken);
              }
            }
          });
    } else {
      logger.warn(
          "Cannot update using an instance of {} when it is not running. Please start the service first.",
          this.getClass().getName());
    }
  }

  @Override
  public boolean isRunning() {
    return isRunning;
  }

  @Override
  public void confirmDelivery(String messageId, String token) {
    databaseHelper.updateDeliveryStatus(true, token, messageId);

    // TODO: remove this and add in the Batched Kafka Sender
    // databaseHelper.removeNotification(messageId, token);
    logger.info("Updated delivery status: {} ", messageId);
  }

  @Override
  public long getNumberOfScheduledNotifications() {
    return databaseHelper.findAllNotifications().size();
  }
}
