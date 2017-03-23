package poker5cardgame;

/**
 * Logging Framework
 */
public class Log {

    public static /* final */ boolean NET_ERROR = true;
    public static /* final */ boolean NET_DEBUG = false;
    public static /* final */ boolean NET_TRACE = false;

    public static /* final */ boolean GAME_ERROR = true;
    public static /* final */ boolean GAME_DEBUG = false;
    public static /* final */ boolean GAME_TRACE = false;

    public static /* final */ boolean IO_ERROR = true;
    public static /* final */ boolean IO_DEBUG = true;
    public static /* final */ boolean IO_TRACE = false;

    public static /* final */ boolean AI_ERROR = true;
    public static /* final */ boolean AI_DEBUG = false;
    public static /* final */ boolean AI_TRACE = false;

    public static void NET_ERROR(String msg) {
        if (NET_ERROR)
            System.err.println(msg);
    }

    public static void NET_DEBUG(String msg) {
        if (NET_DEBUG)
            System.out.println(msg);

    }

    public static void NET_TRACE(String msg) {
        if (NET_TRACE)
            System.out.println("NT-TRACE: " + msg);
    }

    public static void GAME_ERROR(String msg) {
        if (GAME_ERROR)
            System.err.println(msg);
    }

    public static void GAME_DEBUG(String msg) {
        if (GAME_DEBUG)
            System.out.println(msg);

    }

    public static void GAME_TRACE(String msg) {
        if (GAME_TRACE)
            System.out.println("GM-TRACE: " + msg);
    }

    public static void IO_ERROR(String msg) {
        if (IO_ERROR)
            System.err.println(msg);
    }

    public static void IO_DEBUG(String msg) {
        if (IO_DEBUG)
            System.out.println(msg);

    }

    public static void IO_TRACE(String msg) {
        if (IO_TRACE)
            System.out.println("IO-TRACE: " + msg);
    }

    public static void AI_ERROR(String msg) {
        if (AI_ERROR)
            System.err.println(msg);
    }

    public static void AI_DEBUG(String msg) {
        if (AI_DEBUG)
            System.out.println(msg);

    }

    public static void AI_TRACE(String msg) {
        if (AI_TRACE)
            System.out.println("AI-TRACE: " + msg);
    }

}
