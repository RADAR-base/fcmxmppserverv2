package com.wedevol.xmpp;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.radarcns.xmppserver.commandline.CommandLineArgs;
import org.radarcns.xmppserver.factory.SchedulerServiceFactory;
import org.radarcns.xmppserver.service.SchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.wedevol.xmpp.bean.CcsOutMessage;
import com.wedevol.xmpp.server.CcsClient;
import com.wedevol.xmpp.util.MessageMapper;
import com.wedevol.xmpp.util.Util;

/**
 * Entry Point class for the XMPP Server
 *
 * @author Charz++
 */
public class EntryPoint extends CcsClient {

    protected static final Logger logger = LoggerFactory.getLogger(EntryPoint.class);

    public EntryPoint(String projectId, String apiKey, boolean debuggable, String toRegId, String schedulerType) {
        super(projectId, apiKey, debuggable, schedulerType);

        try {
            connect();
        } catch (XMPPException | InterruptedException | KeyManagementException | NoSuchAlgorithmException | SmackException
                | IOException e) {
            logger.error("Error trying to connect. Error: {}", e.getMessage());
        }

        // Send a sample downstream message to a device
        final String messageId = Util.getUniqueMessageId();
        final Map<String, String> dataPayload = new HashMap<String, String>();
        dataPayload.put(Util.PAYLOAD_ATTRIBUTE_MESSAGE, "This is the simple sample message");
        final CcsOutMessage message = new CcsOutMessage(toRegId, messageId, dataPayload);
        final String jsonRequest = MessageMapper.toJsonString(message);
        sendDownstreamMessage(messageId, jsonRequest);

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
            parser.setProgramName("fcmxmppserverv2");
            parser.parse(args);
        } catch (ParameterException exc) {
            parser.usage();
            System.exit(1);
        }

        if (commandLineArgs.help) {
            parser.usage();
            System.exit(0);
        }

        // TODO also look in Environment Variables to initialise these values
        new EntryPoint(commandLineArgs.sender, commandLineArgs.serverKey, false, commandLineArgs.token, commandLineArgs.schedulerType);
    }
}
