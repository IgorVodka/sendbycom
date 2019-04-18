package iu5.sendbycom.gui;

import iu5.sendbycom.application.Swappable;
import iu5.sendbycom.application.connection.event.ConnectionSupportAdapter;
import iu5.sendbycom.application.datatypes.SwapRolesResult;
import iu5.sendbycom.physical.exception.DataTooBigException;

import javax.swing.*;
import java.awt.*;

abstract public class SwappableConnectionSupportAdapter implements ConnectionSupportAdapter {
    private Swappable worker;

    public void setWorker(Swappable worker) {
        this.worker = worker;
    }

    abstract public void onSwapped(long intervalBetweenStopAndRestart);

    @Override
    public void onSwapRolesRequested() {
        EventQueue.invokeLater(() -> {
            int dialogResult = JOptionPane.showConfirmDialog (
                    null,
                    "Другая сторона хочет поменяться ролями. Разрешить?",
                    "Вопрос",
                    JOptionPane.YES_NO_OPTION
            );
            try {
                if (dialogResult == JOptionPane.YES_OPTION){
                    worker.respondSwapRoles(SwapRolesResult.ALLOW);
                    // This side (allower) is the second to reconnect.
                    onSwapped(1000);
                } else {
                    worker.respondSwapRoles(SwapRolesResult.DENY);
                }
            } catch (DataTooBigException e) {
                e.printStackTrace();
            }
        });

    }

    @Override
    public void onSwapRolesResponded(SwapRolesResult result) {
        if (result == SwapRolesResult.ALLOW) {
            // This side (initiator) is the first to reconnect.
            onSwapped(500);
        } else {
            EventQueue.invokeLater(() -> {
                JOptionPane.showMessageDialog(null, "Другая сторона не хочет меняться ролями.");
            });
        }
    }
}
