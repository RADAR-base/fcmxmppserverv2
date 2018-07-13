package com.wedevol.xmpp.server;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.Parameter)
public class CcsClientTest extends GroovyObjectSupport {
    @Parameterized.Parameters
    private String schedulerType() {
        return new String() {
        };
    }

    @Test
    public void testConnect() {
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
