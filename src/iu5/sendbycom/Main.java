package iu5.sendbycom;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import iu5.sendbycom.application.FileClient;
import iu5.sendbycom.application.FileServer;
import iu5.sendbycom.application.combiner.event.*;
import iu5.sendbycom.application.connection.event.ConnectionSupportAdapter;
import iu5.sendbycom.application.datatypes.DirFile;
import iu5.sendbycom.link.Frame;
import iu5.sendbycom.link.FrameReceiver;
import iu5.sendbycom.link.FrameType;
import iu5.sendbycom.physical.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        // Unused...
    }
}
