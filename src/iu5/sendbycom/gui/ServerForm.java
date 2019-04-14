package iu5.sendbycom.gui;

import iu5.sendbycom.Main;
import iu5.sendbycom.application.FileServer;
import iu5.sendbycom.application.connection.event.ConnectionSupportAdapter;
import iu5.sendbycom.physical.Port;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Logger;

public class ServerForm extends JFrame {
    private Port port;

    private JLabel stateLabel;
    private JPanel contentPane;

    ServerForm(String port) {
        super();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.port = new Port(port);

        this.setAlwaysOnTop(true);
        this.setContentPane(contentPane);

        this.setTitle("Сервер на порту " + port);
        this.stateLabel.setText("бла бла");

        launchServer();
    }

    private void launchServer() {
        Thread serverThread = new Thread(new Runnable() {
            public void run() {
                try {
                    FileServer server = new FileServer(
                            Logger.getLogger("server"),
                            port,
                            new ServerConnectionAdapter()
                    );
                    server.listen();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        serverThread.start();

        this.stateLabel.setText("запущен");
    }

    class ServerConnectionAdapter implements ConnectionSupportAdapter {
        @Override
        public void onConnected() {
            stateLabel.setText("Подключено!");
        }

        @Override
        public void onDisconnected() {
            stateLabel.setText("Ожидает соединения...");
        }
    }
}
