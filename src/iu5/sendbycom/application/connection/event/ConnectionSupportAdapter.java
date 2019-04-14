package iu5.sendbycom.application.connection.event;

import java.util.EventListener;

public interface ConnectionSupportAdapter extends EventListener {
    void onConnected();
    void onDisconnected();
}
