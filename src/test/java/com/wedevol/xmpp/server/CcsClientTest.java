package com.wedevol.xmpp.server;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.radarcns.xmppserver.config.Config;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class CcsClientTest{
    @Parameterized.Parameters
    public static Collection<String> schedulerType() {
        return Arrays.asList(
                Config.SCHEDULER_SIMPLE,
                Config.SCHEDULER_MEM,
                Config.SCHEDULER_PERSISTENT);
    }

    @Parameterized.Parameter
    public String schedulerType;

    // Test project on firebase
    public CcsClient ccsClient = new CcsClient("","",true, schedulerType);

    @Test
    public void testConnect() {
        try {
            ccsClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        }
    }
    @Test
    public void testProcessStanza() {

    }

    @Test
    public void testHandlePacketRecieved() {
    }

    @Test
    public void testSendDownstreamMessage() {
    }

}
