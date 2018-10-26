package org.radarcns.xmppserver.commandline;

import com.beust.jcommander.Parameter;
import org.radarcns.xmppserver.config.Config;

/**
 * Paramaters class for parsing command line args for the XMPP Server
 *
 * @author yatharthranjan
 */
public class CommandLineArgs {

    @Parameter(names = {"-s", "--sender-id"}, description = "Sender ID of the FCM project on Firebase.")
    public static String senderId = null;

    @Parameter(names = {"-k", "--server-key"}, description = "Server Key of the FCM project on Firebase.")
    public static String serverKey = null;

    @Parameter(names = {"-ns", "--notification-scheduler"}, description = "The notification scheduler to use with the 'SCHEDULE' action. Options are- simple, in-memory, persistent")
    public static String schedulerType = Config.SCHEDULER_SIMPLE;

    @Parameter(names = {"-p", "--db-path"}, description = "The path where to create db when using database notification schedulers (in-memory and persistent).", validateWith = PathValidator.class)
    public static String dbPath = null;

    @Parameter(names = {"-u", "--db-user"}, description = "The username to use for db when using database notification schedulers (in-memory and persistent).")
    public static String dbUser = "SA";

    @Parameter(names = {"-pa", "--db-password"}, description = "The password to use for db when using database notification schedulers (in-memory and persistent).")
    public static String dbPass = "";

    @Parameter(names = {"-ce", "--cache-expiry"}, description = "The cache expiry in seconds to perform time based eviction of records from cache.")
    public static long cacheExpiry = 30;

    @Parameter(names = {"-ci", "--cache-cleanup-interval"}, description = "The cache cleanup interval in seconds to perform custom time based eviction of records from cache (In cases where there is not operations on the cache). To increase throughput use a lower value.")
    public static long cacheCleanUpInterval = 120;

    @Parameter(names = {"-h", "--help"}, help = true, description = "Display the usage of the program with available options.")
    public boolean help;
}
