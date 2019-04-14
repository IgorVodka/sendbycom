package iu5.sendbycom.application;

import iu5.sendbycom.application.combiner.Combiner;
import iu5.sendbycom.application.combiner.DirCombiner;
import iu5.sendbycom.application.combiner.FileCombiner;
import iu5.sendbycom.application.combiner.event.DirCombinedAdapter;
import iu5.sendbycom.application.combiner.event.FileCombinedAdapter;
import iu5.sendbycom.application.connection.BufferizedReceiver;
import iu5.sendbycom.application.connection.BufferizedSender;
import iu5.sendbycom.application.connection.ConnectionSupporter;
import iu5.sendbycom.application.datatypes.SessionData;
import iu5.sendbycom.application.datatypes.SessionHash;
import iu5.sendbycom.application.connection.event.ConnectionSupportAdapter;
import iu5.sendbycom.application.exception.MetaNotReceivedException;
import iu5.sendbycom.link.ByteUtils;
import iu5.sendbycom.link.Frame;
import iu5.sendbycom.link.FrameReceiver;
import iu5.sendbycom.link.FrameType;
import iu5.sendbycom.physical.exception.DataTooBigException;
import iu5.sendbycom.physical.Port;
import iu5.sendbycom.physical.exception.PortClosedException;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class FileClient {
    private Logger logger;

    private Port serverPort;
    private FrameReceiver receiver;

    private enum ClientState { IDLE, WAITING_FOR_PONG, READY }
    private ClientState clientState;

    private Map<SessionHash, Combiner> combiners;

    private ConnectionSupporter connectionSupporter;

    private BufferizedSender bufferizedSender;
    private BufferizedReceiver bufferizedReceiver;

    public FileClient(Logger logger, Port serverPort, ConnectionSupportAdapter connectionSupportAdapter) {
        this.logger = logger;
        this.serverPort = serverPort;
        this.receiver = new FrameReceiver(serverPort);
        this.clientState = ClientState.IDLE;
        this.bufferizedSender = new BufferizedSender(serverPort);
        this.bufferizedReceiver = new BufferizedReceiver();
        this.connectionSupporter = new ConnectionSupporter(bufferizedSender, serverPort, connectionSupportAdapter);
        this.combiners = new HashMap<>();
    }

    public void connect() throws PortClosedException, DataTooBigException, MetaNotReceivedException, IOException {
        serverPort.open();

        SessionHash handshake = SessionHash.generate();
        bufferizedSender.addFrame(new Frame(FrameType.PING, handshake.getBytes()));
        bufferizedSender.tryToSendFrames();

        clientState = ClientState.WAITING_FOR_PONG;

        while (true) {
            if (clientState == ClientState.READY && connectionSupporter.timeout()) {
                // todo throw exc
                this.clientState = ClientState.IDLE;
                return;
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

                switch (clientState) {
                    case WAITING_FOR_PONG:
                        // Should receive a PONG.
                        if (frame.getType() == FrameType.PONG) {
                            SessionHash receivedHash = new SessionHash(frame.getData());
                            if (handshake.equals(receivedHash)) {
                                // A server should respond with the same handshake as sent to it.
                                // If it's fine, then enter the ready clientState
                                clientState = ClientState.READY;
                                connectionSupporter.startWatcherThread();
                                logger.info("Successfully connected to the server.");
                            } else {
                                // TODO: exception
                                logger.severe("Error! Wrong handshake.");
                                logger.severe("Sent with PING: " + Arrays.toString(handshake.getBytes()));
                                logger.severe("Received with PONG: " + Arrays.toString(frame.getData()));
                            }
                        } else {
                            logUnexpectedFrameType(frame);
                        }
                        break;

                    case READY:
                        // While in ready clientState, many different frame types are supported.
                        // DIR_CONTENTS_META, DIR_CONTENTS_PART, FILE_META, FILE_PART, PARAMS, GOODBYE
                        if (frame.getType() == FrameType.DIR_CONTENTS_META
                                || frame.getType() == FrameType.DIR_CONTENTS_PART
                                || frame.getType() == FrameType.FILE_META
                                || frame.getType() == FrameType.FILE_PART) {
                            SessionData sessionData = SessionData.decode(frame.getData());
                            SessionHash hash = sessionData.getHash();
                            logger.severe("Received a " + frame.getType().name() + "!");

                            if (combiners.containsKey(hash)) {
                                combiners.get(hash).add(frame.getType(), sessionData);
                            } // else todo
                        } else {
                            logUnexpectedFrameType(frame);
                        }

                        break;

                    default:
                        logger.severe("Unknown state: " + clientState.name());
                        logUnexpectedFrameType(frame);
                        break;
                }
            }
        }
    }

    public void requestListDir(String dirPath, DirCombinedAdapter adapter)
            throws DataTooBigException {
        if (clientState != ClientState.READY) {
            // todo exc
            logger.severe("You can only request list dir while in the READY state.");
            return;
        }

        SessionHash hash = SessionHash.generate();
        Frame listDir = new Frame(FrameType.LIST_DIR, new SessionData(hash, dirPath).encode());

        combiners.put(hash, new DirCombiner(adapter));
        bufferizedSender.addFrame(listDir);
    }

    public void requestFile(String filePath, FileCombinedAdapter adapter)
            throws DataTooBigException {
        if (clientState != ClientState.READY) {
            // todo exc
            logger.severe("You can only request file while in the READY state.");
            return;
        }

        SessionHash hash = SessionHash.generate();
        Frame chooseFile = new Frame(FrameType.CHOOSE_FILE, new SessionData(hash, filePath).encode());

        combiners.put(hash, new FileCombiner(adapter));
        bufferizedSender.addFrame(chooseFile);
    }

    private void logUnexpectedFrameType(Frame frame) {
        logger.severe(
                "Received an unexpected frame type: "
                        + frame.getType().name()
                        + " while having clientState: " + clientState.name()
        );
    }
}
