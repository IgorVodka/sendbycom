package iu5.sendbycom.link;

public enum FrameType {
    UNKNOWN,

    BROKEN,

    // Server is listening for this frame.
    // Client is connecting to the server and sending this one.
    PING,

    // Server sends this to the client, so the client may send something.
    PONG,

    // Data is an index of remote frame received
    CONFIRM_RECEIVED,

    // Client tries to choose a directory.
    LIST_DIR,

    // Server may respond with this one...
    DIR_CONTENTS_META,

    // ...or with this one, including a part of file list.
    DIR_CONTENTS_PART,

    // Client chooses a file.
    CHOOSE_FILE,

    // Server may respond with this one...
    FILE_META,

    // ...or with this one, including a file part.
    FILE_PART,

    // Change connection params.
    PARAMS,

    // Request swap roles.
    REQUEST_SWAP_ROLES,

    // Respond to swap roles (0 = deny, 1 = allow).
    RESPOND_SWAP_ROLES,

    // Close the connection.
    GOODBYE;

    public static FrameType fromByte(byte i) {
        final FrameType[] values = FrameType.values();

        // discard if wrong
        if (i < 0 || i >= values.length) {
            System.out.println("Bad data received.");
            return BROKEN;
        }

        return values[i];
    }
}
