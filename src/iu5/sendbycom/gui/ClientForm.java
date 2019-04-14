package iu5.sendbycom.gui;

import iu5.sendbycom.application.CommonParams;
import iu5.sendbycom.application.FileClient;
import iu5.sendbycom.application.FileServer;
import iu5.sendbycom.application.combiner.event.*;
import iu5.sendbycom.application.connection.event.ConnectionSupportAdapter;
import iu5.sendbycom.application.datatypes.DirFile;
import iu5.sendbycom.physical.Port;
import iu5.sendbycom.physical.exception.DataTooBigException;
import iu5.sendbycom.physical.exception.PortClosedException;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Logger;

public class ClientForm extends JFrame {
    private Port port;
    private JPanel contentPane;
    private JLabel stateLabel;
    private JButton requestDirListButton;
    private JTextField dirPathTextField;
    private JList dirList;
    private JList fileList;
    private JButton downloadFileButton;
    private JProgressBar downloadProgressBar;
    private JLabel nettoSpeedLabel;
    private JLabel bruttoSpeedLabel;

    String currentPath;

    private FileClient client;

    ClientForm(String port) {
        super();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.currentPath = "";
        this.port = new Port(port);

        this.downloadProgressBar.setStringPainted(true);
        this.setContentPane(contentPane);
        contentPane.setVisible(false);
        this.setTitle("Клиент на порту " + port);

        launchClient();

        requestDirListButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final String selectedPath = dirPathTextField.getText();

                try {
                    client.requestListDir(selectedPath, new DirCombinedAdapter() {
                        @Override
                        public void onDirCombined(DirCombinedEvent event) {
                            Vector<String> dirs = new Vector<String>();

                            dirs.add("..");

                            Vector<String> files = new Vector<String>();

                            if (!event.isSuccess()) {
                                JOptionPane.showMessageDialog(
                                        new JFrame(),
                                        "Не удалось отобразить список файлов",
                                        "Ошибка!",
                                        JOptionPane.ERROR_MESSAGE
                                );
                                return;
                            }

                            // todo use model
                            for (DirFile dirFile : event.getFiles()) {
                                if (dirFile.isDirectory()) {
                                    dirs.add(dirFile.getName());
                                } else {
                                    files.add(dirFile.getName());
                                }
                            }

                            dirPathTextField.setText(selectedPath);
                            currentPath = selectedPath;

                            dirList.setListData(dirs);
                            fileList.setListData(files);
                        }
                    });
                } catch (DataTooBigException e1) {
                    e1.printStackTrace(); // todo
                }
            }
        });

        dirList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }

                String selectedPath = (String) dirList.getSelectedValue();
                String newPath = FilenameUtils.concat(currentPath, selectedPath);

                dirPathTextField.setText(newPath);
            }
        });

        downloadFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fileList.isSelectionEmpty()) {
                    JOptionPane.showMessageDialog(
                            new JFrame(),
                            "Сначала выберите файл в правом столбце!",
                            "Ошибка!",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                String sourceFileName = (String) fileList.getSelectedValue();
                final String sourcePath = FilenameUtils.concat(currentPath, sourceFileName);
                String destinationPath = "";

                JFileChooser chooser = new JFileChooser();
                chooser.setSelectedFile(new File(sourceFileName));

                int choosingResult = chooser.showSaveDialog(ClientForm.this);
                if (choosingResult == JFileChooser.APPROVE_OPTION) {
                    destinationPath = chooser.getSelectedFile().getPath();
                }
                if (choosingResult == JFileChooser.CANCEL_OPTION) {
                    JOptionPane.showMessageDialog(
                            new JFrame(),
                            "Не сохраняем...",
                            "Ошибка!",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                JOptionPane.showMessageDialog(
                        new JFrame(),
                        "Будем загружать файл: " + sourcePath + ", и писать сюда: " + destinationPath
                );

                downloadProgressBar.setValue(0);

                try {
                    final FileOutputStream stream = new FileOutputStream(destinationPath);

                    Thread downloader = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                client.requestFile(sourcePath, new FileCombinedAdapter() {
                                    long timeBegan;
                                    int totalParts;

                                    @Override
                                    public void onFileStarted(int totalParts) {
                                        this.timeBegan = new Date().getTime();
                                        this.totalParts = totalParts;

                                        requestDirListButton.setEnabled(false);
                                        downloadFileButton.setEnabled(false);

                                        downloadProgressBar.setMinimum(0);
                                        downloadProgressBar.setMaximum(totalParts - 1);
                                    }

                                    @Override
                                    public void onFileCombined(FileCombinedEvent event) throws IOException {
                                        requestDirListButton.setEnabled(true);
                                        downloadFileButton.setEnabled(true);

                                        if (!event.isSuccess()) {
                                            JOptionPane.showMessageDialog(
                                                    new JFrame(),
                                                    "Ошибка загрузки файла!",
                                                    "Ошибка!",
                                                    JOptionPane.ERROR_MESSAGE
                                            );
                                            downloadProgressBar.setValue(0);
                                            return;
                                        }

                                        JOptionPane.showMessageDialog(new JFrame(), "Файл успешно загружен!");
                                        stream.close();
                                    }

                                    @Override
                                    public void onFilePartReceived(FilePartReceivedEvent event) throws IOException {
                                        System.out.println("FILE PART REALLY RECEIVED");

                                        downloadProgressBar.setValue(downloadProgressBar.getValue() + 1);
                                        stream.write(event.getPart());

                                        int partsReceived = downloadProgressBar.getValue();
                                        int bytesReceivedNetto = partsReceived * CommonParams.PART_SIZE;

                                        // +2 frame, +4 session, * 7/4 hamming
                                        int bytesReceivedBrutto = bytesReceivedNetto * 7/4 + partsReceived * 6;

                                        long timeDiff = Math.max(1, new Date().getTime() - timeBegan);

                                        // 8000 = bits/byte * seconds/ms
                                        nettoSpeedLabel.setText((8000 * bytesReceivedNetto / timeDiff) + " бит/с");
                                        bruttoSpeedLabel.setText((8000 * bytesReceivedBrutto / timeDiff) + " бит/с");
                                    }
                                });
                            } catch (DataTooBigException e1) {
                                e1.printStackTrace(); // todo
                            }
                        }
                    });
                    downloader.start();
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace(); // todo

                }
            }
        });
    }

    private void launchClient() {
        Thread clientThread = new Thread(() -> {
            try {
                client = new FileClient(
                        Logger.getLogger("client"),
                        port,
                        new ClientConnectionAdapter()
                );
                client.connect();
                Thread.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace(); // todo
            }
            });

        clientThread.start();
    }

    class ClientConnectionAdapter implements ConnectionSupportAdapter {
        @Override
        public void onConnected() {
            contentPane.setVisible(true);
            stateLabel.setText("Подключено!");
        }

        @Override
        public void onDisconnected() {
            JOptionPane.showMessageDialog(
                    new JFrame(),
                    "Соединение потеряно :(",
                    "Ошибка!",
                    JOptionPane.ERROR_MESSAGE
            );

            dispose();
        }
    }
}
