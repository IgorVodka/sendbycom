package iu5.sendbycom.gui;

import iu5.sendbycom.Main;
import iu5.sendbycom.application.FileServer;
import iu5.sendbycom.application.Swappable;
import iu5.sendbycom.application.connection.event.ConnectionSupportAdapter;
import iu5.sendbycom.physical.Port;
import iu5.sendbycom.physical.exception.DataTooBigException;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.util.logging.Logger;

public class ServerForm extends JFrame {
    private Port port;

    private JLabel stateLabel;
    private JPanel contentPane;
    private JButton swapRolesButton;

    private Thread serverThread;
    private FileServer server;

    ServerForm(String port) {
        super();

        setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.port = new Port(port);

        this.setAlwaysOnTop(true);
        this.setContentPane(contentPane);

        this.setTitle("Сервер на порту " + port);
        this.stateLabel.setText("бла бла");

        this.swapRolesButton.addActionListener(e -> {
            try {
                server.requestSwapRoles();
            } catch (DataTooBigException e1) {
                e1.printStackTrace();
            }
        });

        launchServer();
    }

    private void launchServer() {
        serverThread = new Thread(() -> {
            try {
                ServerConnectionAdapter adapter = new ServerConnectionAdapter();
                server = new FileServer(
                        Logger.getLogger("server"),
                        port,
                        adapter
                );
                adapter.setWorker(server);
                server.listen();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        serverThread.start();

        this.stateLabel.setText("Ожидает...");
    }

    class ServerConnectionAdapter extends SwappableConnectionSupportAdapter {
        @Override
        public void onConnected() {
            stateLabel.setText("Подключено!");
        }

        @Override
        public void onDisconnected() {
            stateLabel.setText("Ожидает соединения...");
            serverThread.interrupt();
            launchServer();
        }

        @Override
        public void onSwapped(long intervalBetweenStopAndRestart) {
            EventQueue.invokeLater(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                server.stopWatcherThread();
                serverThread.interrupt();

                port.close();
                setVisible(false);

                try {
                    Thread.sleep(intervalBetweenStopAndRestart);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                ClientForm form = new ClientForm(port.getName());
                form.setLocation(getLocation());
                form.pack();
                form.setVisible(true);

                dispose();
            });
        }
    }
}
