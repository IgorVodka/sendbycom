import com.fazecast.jSerialComm.SerialPort;

public class Main {
    public static void main(String[] args) {
        SerialPort megaPort = SerialPort.getCommPort("/dev/tnt3");
        System.out.println(megaPort.isOpen());

        SerialPort[] ports = SerialPort.getCommPorts();

        System.out.println("Ports:");

        for (SerialPort port : ports) {
            System.out.println(port.getDescriptivePortName());
        }
    }
}
