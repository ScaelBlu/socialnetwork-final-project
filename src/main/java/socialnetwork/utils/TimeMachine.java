package socialnetwork.utils;

import java.time.LocalDateTime;

public class TimeMachine {

    private static final TimeMachine INSTANCE = new TimeMachine();
    private LocalDateTime now;

    private TimeMachine() {
    }

    public static LocalDateTime now() {
        return INSTANCE.now;
    }

    public static void set(LocalDateTime now) {
        INSTANCE.now = now;
    }

    public static boolean isSet() {
        return INSTANCE.now != null;
    }

    public static void clear() {
        INSTANCE.now = null;
    }
}
