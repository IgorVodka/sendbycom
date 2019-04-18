package iu5.sendbycom.application.connection.event;

import iu5.sendbycom.application.datatypes.SwapRolesResult;

import java.util.EventListener;

public interface ConnectionSupportAdapter extends EventListener {
    void onConnected();
    void onSwapRolesRequested();
    void onSwapRolesResponded(SwapRolesResult result);
    void onDisconnected();
}
