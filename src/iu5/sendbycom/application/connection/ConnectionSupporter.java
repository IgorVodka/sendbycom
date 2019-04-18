package iu5.sendbycom.application.connection;

import iu5.sendbycom.application.CommonParams;
import iu5.sendbycom.application.connection.event.ConnectionSupportAdapter;
import iu5.sendbycom.link.ByteUtils;
import iu5.sendbycom.link.Frame;
import iu5.sendbycom.link.FrameType;
import iu5.sendbycom.physical.Port;
import iu5.sendbycom.physical.exception.DataTooBigException;
import iu5.sendbycom.physical.exception.PortClosedException;

import java.util.Date;

public class ConnectionSupporter {
    private Thread thread;
    private BufferizedSender sender;
    private Port port;
    private long lastOnlineTime;
    private int lastReceivedFrameIndex;
    private ConnectionSupportAdapter connectionSupportAdapter;

    public ConnectionSupporter(BufferizedSender sender, Port port, ConnectionSupportAdapter connectionSupportAdapter) {
        this.sender = sender;
        this.port = port;
        this.lastOnlineTime = new Date().getTime();
        this.connectionSupportAdapter = connectionSupportAdapter;
    }

    public void startWatcherThread() {
        updateLastOnline();
        connectionSupportAdapter.onConnected();

        thread = new Thread(() -> {
            while (true) {
                try {
                    // without sender, directly to the port, because URGENT!
                    Frame stillAliveFrame = new Frame(
                        FrameType.CONFIRM_RECEIVED,
                        0,
                        ByteUtils.intToBytes(lastReceivedFrameIndex)
                    );
                    port.sendFrame(stillAliveFrame);

                    System.out.println(
                        "Sending a CONFIRM_RECEIVED frame at " + new Date().toGMTString()
                        + ", confirming frame index (remote) #" + lastReceivedFrameIndex
                    );

                    if (timeout()) {
                        System.out.println("OOOPS! Timeout.");
                        connectionSupportAdapter.onDisconnected();
                        return;
                    }
                    Thread.sleep(CommonParams.STILL_ALIVE_INTERVAL);
                } catch (DataTooBigException | InterruptedException | PortClosedException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    public void stopWatcherThread() {
        thread.stop();
    }

    public void updateLastOnline() {
        System.out.println("Updating last online at " + new Date().toGMTString());
        this.lastOnlineTime = new Date().getTime();
    }

    // last received frame index = an index of frame that is guaranteed to have been received
    // so you got to set it when you receive any frame
    // later it will be sent inside CONFIRM_RECEIVED
    public void setLastReceivedLocalFrameIndex(int index) {
        if (this.lastReceivedFrameIndex <= index) {
            System.out.println("Set last received frame index to (remote) #" + index);
            this.lastReceivedFrameIndex = index;
        }
    }

    public boolean timeout() {
        return new Date().getTime() - lastOnlineTime > CommonParams.CONNECTION_TIMEOUT;
    }
}
