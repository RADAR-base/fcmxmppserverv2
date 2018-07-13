package org.radarcns.xmppserver.commandline;

import com.beust.jcommander.Parameter;

public class CommandLineArgs {

    @Parameter(names = { "-s", "--sender-id" }, description = "Sender ID of the FCM project on Firebase.")
    public String sender = "csv";

    @Parameter(names = { "-k", "--server-key" }, description = "Server Key of the FCM project on Firebase.")
    public String serverKey = "none";

    // Default set to false because causes loss of records from Biovotion data. https://github.com/RADAR-base/Restructure-HDFS-topic/issues/16
    @Parameter(names = { "-t", "--token" }, description = "The default receipient token to send downstream messages to. This is optional as the app recreated the token from the upstream messgaes it receives.")
    public String token;

    @Parameter(names = { "-ns", "--notification-scheduler" }, description = "The notification scheduler to use with the 'SCHEDULE' action. Options are- simple, in-memory, persistent")
    public String schedulerType;

    @Parameter(names = { "-p", "--db-path"}, description = "The path where to create db when using database notification schedulers (in-memory and persistent).", validateWith = PathValidator.class)
    public String dbPath;

    @Parameter(names = { "-h", "--help"}, help = true, description = "Display the usage of the program with available options.")
    public boolean help;
}
