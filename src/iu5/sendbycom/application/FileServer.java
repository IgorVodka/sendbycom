package iu5.sendbycom.application;

import iu5.sendbycom.link.bufferized.BufferizedReceiver;
import iu5.sendbycom.link.bufferized.BufferizedSender;
import iu5.sendbycom.application.connection.ConnectionSupporter;
import iu5.sendbycom.application.datatypes.SessionData;
import iu5.sendbycom.application.connection.event.ConnectionSupportAdapter;
import iu5.sendbycom.application.message.DirContents;
import iu5.sendbycom.application.message.FileContents;
import iu5.sendbycom.link.ByteUtils;
import iu5.sendbycom.link.Frame;
import iu5.sendbycom.link.FrameReceiver;
import iu5.sendbycom.link.FrameType;
import iu5.sendbycom.physical.exception.DataTooBigException;
import iu5.sendbycom.physical.Port;
import iu5.sendbycom.physical.exception.PortClosedException;

import java.util.logging.Logger;

public class FileServer {
    private Logger logger;

    private Port clientPort;
    private FrameReceiver receiver;

    private enum ServerState { IDLE, READY }
    private ServerState serverState;

    private ConnectionSupporter connectionSupporter;

    private BufferizedSender bufferizedSender; // todo rename to bufferizedSender
    private BufferizedReceiver bufferizedReceiver;

    public FileServer(Logger logger, Port clientPort, ConnectionSupportAdapter connectionSupportAdapter) {
        this.logger = logger;
        this.clientPort = clientPort;
        this.receiver = new FrameReceiver(clientPort);
        this.serverState = ServerState.IDLE;
        this.bufferizedSender = new BufferizedSender(clientPort);
        this.bufferizedReceiver = new BufferizedReceiver();
        this.connectionSupporter = new ConnectionSupporter(bufferizedSender, clientPort, connectionSupportAdapter);
    }

    public void listen() throws PortClosedException, DataTooBigException {
        clientPort.open();

        while (true) {
            if (serverState == ServerState.READY && connectionSupporter.timeout()) {
                // todo throw exc
                this.serverState = ServerState.IDLE;
            }

            bufferizedSender.tryToSendFrames();

            if (receiver.hasData()) {
                connectionSupporter.updateLastOnline();

                Frame frameJustReceived = receiver.nextFrame();
                if (frameJustReceived.getType() == FrameType.CONFIRM_RECEIVED) {
                    int confirmedIndex = ByteUtils.bytesToInt(frameJustReceived.getData());
                    System.out.println("Trying to mark as received remotely (local) #" + confirmedIndex);
                    bufferizedSender.markAllUpToLocalIndexAsReceivedRemotely(ByteUtils.bytesToInt(frameJustReceived.getData()));
                    continue;
                }

                bufferizedReceiver.addFrame(frameJustReceived);
            }

            Frame frame = bufferizedReceiver.getOrderedFrameOrNull();
            if (frame != null) {
                connectionSupporter.setLastReceivedLocalFrameIndex(frame.getIndex());

                switch (serverState) {
                    case IDLE:
                        // Should receive a PING.
                        if (frame.getType() == FrameType.PING) {
                            // ...and respond with a PONG containing the same data.
                            assert frame.getData().length == 4;

                            bufferizedSender.addFrame(new Frame(FrameType.PONG, frame.getData()));
                            serverState = ServerState.READY;
                            connectionSupporter.startWatcherThread();

                            logger.info("Successfully connected with the client.");
                        } else {
                            logUnexpectedFrameType(frame);
                        }
                        break;

                    case READY:
                        // While in ready serverState, many different frame types are supported.
                        // LIST_DIR, CHOOSE_FILE, PARAMS, GOODBYE

                        if (frame.getType() == FrameType.LIST_DIR) {
                            SessionData data = SessionData.decode(frame.getData());

                            String dirPath = new String(data.getBody());
                            DirContents dirContents = new DirContents(dirPath, data.getHash());
                            logger.info("Requested the " + dirPath + " directory list.");
                            bufferizedSender.addFrames(dirContents.getFrames());
                        } else if (frame.getType() == FrameType.CHOOSE_FILE) {
                            SessionData data = SessionData.decode(frame.getData());

                            String filePath = new String(data.getBody());
                            FileContents fileContents = new FileContents(filePath, data.getHash());
                            logger.info("Requested the " + filePath + " file.");
                            bufferizedSender.addFrames(fileContents.getFrames());
                        } else {
                            logUnexpectedFrameType(frame);
                        }

                        break;
                }
            }
        }
    }

    // TODO move to parent class, among with handlers for PARAMS and GOODBYE
    private void logUnexpectedFrameType(Frame frame) {
        logger.severe(
                "Received an unexpected frame type: "
                + frame.getType().name()
                + " while having serverState: " + serverState.name()
        );
    }
}
