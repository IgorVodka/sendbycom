package iu5.sendbycom.gui;

import com.fazecast.jSerialComm.SerialPort;
import iu5.sendbycom.physical.Port;

import javax.swing.*;
import java.awt.event.*;

public class ConnectDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonServer;
    private JButton buttonClient;
    private JComboBox portsComboBox;

    public ConnectDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonServer);

        populatePortsComboBox();

        buttonServer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onServerChosen();
            }
        });
        buttonClient.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onClientChosen();
            }
        });
    }

    private void populatePortsComboBox() {
        for(Port port : Port.listAvailablePorts()) {
            String name = port.getName();

            if (port.isOpen()) {
                name = name + " (открыт)";
            }

            this.portsComboBox.addItem(name);
        }
    }

    private void onServerChosen() {
        ServerForm form = new ServerForm((String) this.portsComboBox.getSelectedItem());
        form.pack();
        form.setVisible(true);
        dispose();
    }

    private void onClientChosen() {
        ClientForm form = new ClientForm((String) this.portsComboBox.getSelectedItem());
        form.pack();
        form.setVisible(true);

        dispose();
    }
}
