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
        setUIFont(new javax.swing.plaf.FontUIResource("Segoe UI", Font.PLAIN, 18));
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> createGUI());
    }

    public static void setUIFont(javax.swing.plaf.FontUIResource f){
        java.util.Enumeration keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get (key);
            if (value instanceof javax.swing.plaf.FontUIResource)
                UIManager.put (key, f);
        }
    }

    private static void createGUI() {
        ConnectDialog dialog = new ConnectDialog();
        dialog.pack();
        dialog.setVisible(true);
    }
}
