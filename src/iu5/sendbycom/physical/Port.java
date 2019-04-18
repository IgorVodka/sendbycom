package iu5.sendbycom.physical;

import com.fazecast.jSerialComm.SerialPort;
import iu5.sendbycom.link.Frame;
import iu5.sendbycom.link.hamming.Hamming;
import iu5.sendbycom.physical.exception.PortClosedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Port {
    private SerialPort port;

    public Port(SerialPort port) {
        this.port = port;
    }

    public Port(String port) {
        this.port = SerialPort.getCommPort(port);
    }

    public static List<Port> listAvailablePorts() {
        SerialPort[] ports = SerialPort.getCommPorts();

        List<Port> result = new ArrayList<Port>();
        for (SerialPort port : ports) {
            result.add(new Port(port));
        }

        return result;
    }

    public String getName() {
        return port.getSystemPortName();
    }

    public String getDescription() {
        return port.getPortDescription();
    }

    public void open() {
        port.openPort();
        port.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 3000, 3000);
    }

    public boolean isOpen() {
        return port.isOpen();
    }

    public int writeBytes(byte[] bytes) throws PortClosedException {
        if (!isOpen()) {
            throw new PortClosedException(getName());
        }

        System.out.println("Sending: " + Arrays.toString(bytes)); // todo

        return port.writeBytes(bytes, bytes.length);
    }

    // todo: IMPORTANT. move this to FrameSender. levels should be divided
    public int sendFrame(Frame frame) throws PortClosedException {
        return writeBytes(frame.toBytes());
    }

    public int readBytes(byte[] bytes, int bytesToRead) throws PortClosedException {
        if (!isOpen()) {
            throw new PortClosedException(getName());
        }
        int count = port.readBytes(bytes, bytesToRead);
        System.out.println("Receiving: " + Arrays.toString(Arrays.copyOfRange(bytes, 0, bytesToRead))); // todo
        return count;
    }

    public int bytesAvailable() {
        return port.bytesAvailable();
    }

    public void setBaudRate(int baudRate) {
        port.setBaudRate(baudRate);
    }

    public int getBaudRate() {
        return port.getBaudRate();
    }

    public void setStopBitsNumber(StopBitsNumber stopBitsNumber) {
        int stopBitsNumberResult = 0;

        switch(stopBitsNumber) {
            case ONE:
                stopBitsNumberResult = SerialPort.ONE_STOP_BIT;
                break;
            case ONE_AND_HALF:
                stopBitsNumberResult = SerialPort.ONE_POINT_FIVE_STOP_BITS;
                break;
            case TWO:
                stopBitsNumberResult = SerialPort.TWO_STOP_BITS;
                break;
        }

        port.setNumStopBits(stopBitsNumberResult);
    }

    public StopBitsNumber getStopBitsNumber() throws UnknownPortParameter  {
        switch (port.getNumStopBits()) {
            case SerialPort.ONE_STOP_BIT:
                return StopBitsNumber.ONE;
            case SerialPort.ONE_POINT_FIVE_STOP_BITS:
                return StopBitsNumber.ONE_AND_HALF;
            case SerialPort.TWO_STOP_BITS:
                return StopBitsNumber.TWO;
            default:
                throw new UnknownPortParameter("stop bits", port.getNumStopBits());
        }
    }

    public void setParity(PortParity parity) {
        int parityResult = 0;

        switch(parity) {
            case NONE:
                parityResult = SerialPort.NO_PARITY;
                break;
            case EVEN:
                parityResult = SerialPort.EVEN_PARITY;
                break;
            case ODD:
                parityResult = SerialPort.ODD_PARITY;
                break;
            case MARK:
                parityResult = SerialPort.MARK_PARITY;
                break;
            case SPACE:
                parityResult = SerialPort.SPACE_PARITY;
                break;
        }

        port.setParity(parityResult);
    }

    public PortParity getParity() throws UnknownPortParameter {
        switch(port.getParity()) {
            case SerialPort.NO_PARITY:
                return PortParity.NONE;
            case SerialPort.EVEN_PARITY:
                return PortParity.EVEN;
            case SerialPort.ODD_PARITY:
                return PortParity.ODD;
            case SerialPort.MARK_PARITY:
                return PortParity.MARK;
            case SerialPort.SPACE_PARITY:
                return PortParity.SPACE;
            default:
                throw new UnknownPortParameter("parity", port.getParity());
        }
    }

    public void close() {
        port.setBreak();
        port.closePort();
    }
}
