package iu5.sendbycom.gui;

import iu5.sendbycom.link.hamming.Hamming;
import iu5.sendbycom.link.hamming.HammingEncoder;
import iu5.sendbycom.link.hamming.HammingReceiver;
import iu5.sendbycom.link.hamming.Message;
import sun.security.util.BitArray;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.BitSet;

public class Main {
    public static void main(String[] args) {
        byte[] encoded = Hamming.encode(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 });
        byte[] decoded = Hamming.decode(encoded);

        System.out.println(Arrays.toString(decoded));

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                createGUI();
            }
        });
    }

    private static void createGUI() {
        ConnectDialog dialog = new ConnectDialog();
        dialog.pack();
        dialog.setVisible(true);
    }
}
