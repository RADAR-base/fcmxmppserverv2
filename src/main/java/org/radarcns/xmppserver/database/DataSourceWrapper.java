package org.radarcns.xmppserver.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;

public class DataSourceWrapper {

    private final DataSource dataSource;
    private static final Logger logger = LoggerFactory.getLogger(DataSourceWrapper.class);

    public DataSourceWrapper(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void createTableForScheduler() {
        try {
            // Create table for db-scheduler
            dataSource.getConnection().createStatement()
                    .executeUpdate(
                            "create table if not exists scheduled_tasks (\n" +
                                    "  task_name varchar(40) not null,\n" +
                                    "  task_instance varchar(500) not null,\n" +
                                    "  task_data blob,\n" +
                                    "  execution_time timestamp(6) not null,\n" +
                                    "  picked BOOLEAN not null,\n" +
                                    "  picked_by varchar(50),\n" +
                                    "  last_success timestamp(6) null,\n" +
                                    "  last_failure timestamp(6) null,\n" +
                                    "  last_heartbeat timestamp(6) null,\n" +
                                    "  version BIGINT not null,\n" +
                                    "  PRIMARY KEY (task_name, task_instance)\n" +
                                    ")");

        } catch (SQLException e) {
            logger.error("Cannot start the service {} due to {}", this.getClass().getName(), e);
            e.printStackTrace();
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void close() {
        try {
            dataSource.getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
