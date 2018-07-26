package com.wedevol.xmpp;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CountDownLatch;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.radarcns.xmppserver.commandline.CommandLineArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.wedevol.xmpp.server.CcsClient;
import org.radarcns.xmppserver.config.Config;

/**
 * Entry Point class for the XMPP Server
 *
 * @author yatharthranjan
 */
public class EntryPoint extends CcsClient {

    protected static final Logger logger = LoggerFactory.getLogger(EntryPoint.class);

    private EntryPoint(String projectId, String apiKey, boolean debuggable, String schedulerType) {
        super(projectId, apiKey, debuggable, schedulerType);

        try {
            connect();
        } catch (XMPPException | InterruptedException | KeyManagementException | NoSuchAlgorithmException | SmackException
                | IOException e) {
            logger.error("Error trying to connect. Error: {}", e.getMessage());
        }

        /*        // Send a sample downstream message to a device
        final String messageId = Util.getUniqueMessageId();
        final Map<String, String> dataPayload = new HashMap<String, String>();
        dataPayload.put(Util.PAYLOAD_ATTRIBUTE_MESSAGE, "This is the simple sample message");
        final CcsOutMessage message = new CcsOutMessage(toRegId, messageId, dataPayload);
        final String jsonRequest = MessageMapper.toJsonString(message);
        //sendDownstreamMessage(messageId, jsonRequest);
        */
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            latch.await();
        } catch (InterruptedException e) {
            logger.error("An error occurred while latch was waiting. Error: {}", e.getMessage());
        }
    }

    public static void main(String[] args) throws SmackException, IOException {
        final CommandLineArgs commandLineArgs = new CommandLineArgs();
        final JCommander parser = JCommander.newBuilder().addObject(commandLineArgs).build();
        try {
            parser.setProgramName("radar-xmppserver");
            parser.parse(args);
        } catch (ParameterException exc) {
            parser.usage();
            System.exit(1);
        }

        if (commandLineArgs.help) {
            parser.usage();
            System.exit(0);
        }


        // Check if System environment variables exist and overwrite the values

        commandLineArgs.schedulerType = System.getenv("RADAR_XMPP_SCHEDULER_TYPE") != null ?
                System.getenv("RADAR_XMPP_SCHEDULER_TYPE") : commandLineArgs.schedulerType;

        commandLineArgs.dbPath = System.getenv("RADAR_XMPP_DB_PATH") != null ?
                System.getenv("RADAR_XMPP_DB_PATH") : commandLineArgs.dbPath;

        commandLineArgs.sender = System.getenv("RADAR_XMPP_FCM_SENDER_KEY") != null ?
                System.getenv("RADAR_XMPP_FCM_SENDER_KEY") : commandLineArgs.sender ;

        commandLineArgs.serverKey = System.getenv("RADAR_XMPP_FCM_SERVER_KEY") != null ?
                System.getenv("RADAR_XMPP_FCM_SERVER_KEY") : commandLineArgs.serverKey;

        commandLineArgs.dbUser = System.getenv("RADAR_XMPP_DB_USER") != null ?
                System.getenv("RADAR_XMPP_DB_USER") : commandLineArgs.dbUser;

        commandLineArgs.dbPass = System.getenv("RADAR_XMPP_DB_PASS") != null ?
                System.getenv("RADAR_XMPP_DB_PASS") : commandLineArgs.dbPass;


        if(! commandLineArgs.schedulerType.equals(Config.SCHEDULER_SIMPLE)
                && (commandLineArgs.dbPath == null || commandLineArgs.dbPath.isEmpty())) {
            switch (commandLineArgs.schedulerType) {
                case Config.SCHEDULER_MEM:
                    commandLineArgs.dbPath = "notificationDB";
                    break;

                case Config.SCHEDULER_PERSISTENT:
                    commandLineArgs.dbPath = "/usr/hsql/notification";
                    break;
            }
        }

        if(commandLineArgs.sender == null || commandLineArgs.sender.isEmpty()
                || commandLineArgs.serverKey == null || commandLineArgs.serverKey.isEmpty()) {
            parser.usage();
            logger.error("ERROR: Please specify the SENDER KEY and SERVER KEY " +
                    "either via commandline args or via environment variables. Use -h or --help for more info.");
        }

        new EntryPoint(commandLineArgs.sender, commandLineArgs.serverKey, false, commandLineArgs.schedulerType);
    }

}
