package org.radarcns.xmppserver.commandline;

import com.beust.jcommander.Parameter;
import org.radarcns.xmppserver.config.Config;

/**
 * Paramaters class for parsing command line args for the XMPP Server
 *
 * @author yatharthranjan
 */
public class CommandLineArgs {

    @Parameter(names = { "-s", "--sender-id" }, description = "Sender ID of the FCM project on Firebase.")
    public static String sender = null;

    @Parameter(names = { "-k", "--server-key" }, description = "Server Key of the FCM project on Firebase.")
    public static String serverKey = null;

    @Parameter(names = { "-ns", "--notification-scheduler" }, description = "The notification scheduler to use with the 'SCHEDULE' action. Options are- simple, in-memory, persistent")
    public static String schedulerType = Config.SCHEDULER_SIMPLE;

    @Parameter(names = { "-p", "--db-path"}, description = "The path where to create db when using database notification schedulers (in-memory and persistent).", validateWith = PathValidator.class)
    public static String dbPath = null;

    @Parameter(names = { "-u", "--db-user"}, description = "The username to use for db when using database notification schedulers (in-memory and persistent).")
    public static String dbUser = "SA";

    @Parameter(names = { "-pa", "--db-password"}, description = "The password to use for db when using database notification schedulers (in-memory and persistent).")
    public static String dbPass = "";

    @Parameter(names = { "-h", "--help"}, help = true, description = "Display the usage of the program with available options.")
    public boolean help;
}
