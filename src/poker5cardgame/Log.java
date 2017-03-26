package poker5cardgame;

/**
 * Logging Framework
 */
public class Log {
    
    
    
    public static /* final */ boolean FANCY_CLIENT = true;
    
    public static /* final */ boolean NET_ERROR = true;
    public static /* final */ boolean NET_DEBUG = false;
    public static /* final */ boolean NET_TRACE = false;
    
    public static /* final */ boolean KB_ERROR = true;
    public static /* final */ boolean KB_DEBUG = false;
    public static /* final */ boolean KB_TRACE = false;

    public static /* final */ boolean GAME_ERROR = true;
    public static /* final */ boolean GAME_DEBUG = false;
    public static /* final */ boolean GAME_TRACE = false;

    public static /* final */ boolean IO_ERROR = true;
    public static /* final */ boolean IO_DEBUG = false;
    public static /* final */ boolean IO_TRACE = false;

    public static /* final */ boolean AI_ERROR = true;
    public static /* final */ boolean AI_DEBUG = false;
    public static /* final */ boolean AI_TRACE = false;
    
    
    public static void NET_ERROR(String msg) {
        if (NET_ERROR)
            System.err.println("NET_ERROR: "+msg);
    }

    public static void NET_DEBUG(String msg) {
        if (NET_DEBUG)
            System.out.println("NET_DEBUG: "+msg);
    }

    public static void NET_TRACE(String msg) {
        if (NET_TRACE)
            System.out.println("NET_TRACE: " + msg);
    }
    
    public static void KB_ERROR(String msg) {
        if (KB_ERROR)
            System.err.println("KB_ERROR: "+msg);
    }

    public static void KB_DEBUG(String msg) {
        if (KB_DEBUG)
            System.out.println("KB_DEBUG: "+msg);
    }

    public static void KB_TRACE(String msg) {
        if (KB_TRACE)
            System.out.println("KB_TRACE: " + msg);
    }

    public static void GAME_ERROR(int id, String msg) {
        if (GAME_ERROR)
            System.err.println("GAME_ERROR: " + id + " | " + msg);
    }

    public static void GAME_DEBUG(int id, String msg) {
        if (GAME_DEBUG)
            System.out.println("GAME_DEBUG " + id + " | " + msg);

    }
    
    public static void GAME_DEBUG(String msg) {
        if (GAME_DEBUG)
            System.out.println("GAME_DEBUG: "+msg);

    }

    public static void GAME_TRACE(int id, String msg) {
        if (GAME_TRACE)
            System.out.println("GAME_TRACE: " + id + " | " + msg);
    }

    public static void IO_ERROR(String msg) {
        if (IO_ERROR)
            System.err.println("IO_ERROR: " + msg);
    }

    public static void IO_DEBUG(String msg) {
        if (IO_DEBUG)
            System.out.println("IO_DEBUG: " + msg);

    }

    public static void IO_TRACE(String msg) {
        if (IO_TRACE)
            System.out.println("IO_TRACE: " + msg);
    }

    public static void AI_ERROR(String msg) {
        if (AI_ERROR)
            System.err.println("AI_ERROR: "+msg);
    }

    public static void AI_DEBUG(String msg) {
        if (AI_DEBUG)
            System.out.println("AI_DEBUG: "+msg);

    }

    public static void AI_TRACE(String msg) {
        if (AI_TRACE)
            System.out.println("AI_TRACE: " + msg);
    }
    
    public static void FANCY_CLIENT(String msg, Format... formats) {
        if (FANCY_CLIENT) {
            String formatStr = "";
            for (Format format : formats) {
                formatStr += format.code;
            }
            formatStr += msg + Format.RESET.code;
            System.out.print(formatStr);
        }
    }

    public static void FANCY_CLIENT_RAINBOW(String msg) {
        String[] chars = msg.split("");
        Format[] rainbow = Format.getRainbowColors();

        int rIndex = 0;
        for (String c : chars) {
            FANCY_CLIENT(c, Format.BOLD, rainbow[rIndex]);
            rIndex = (rIndex + 1) % rainbow.length;
        }
    }

    public enum Format {
        // format
        BOLD("\u001B[1m"),
        DIM("\u001B[2m"),
        UNDERLINE("\u001B[4m"),
        OVERSTRIKE("\u001B[9m"),
        // colors
        RED("\u001B[31m"),
        ORANGE("\u001B[38;5;214m"),
        YELLOW("\u001B[38;5;11m"),
        GREEN("\u001B[32m"),
        BLUE("\u001B[34m"),
        CYAN("\u001B[36m"),
        PURPLE("\u001B[38;5;127m"),
        PINK("\u001B[35m"),
        GRAY("\u001B[38;5;7m"),
        // suit symbols
        CLUBS("\u2663"),
        DIAMONDS("\u001B[31m\u2666\u001B[0m"),
        HEARTS("\u001B[31m\u2665\u001B[0m"),
        SPADES("\u2660"),
        // reset
        RESET("\u001B[0m");

        public String code;

        Format(String code) {
            this.code = code;
        }

        public static String getCodeFromName(String name)
        {
            for(Format format : Format.values())
            {
                if(format.name().equals(name))
                    return format.code;
            }
            return null;
        }
        
        public static Format[] getRainbowColors() {
            return new Format[]{RED, ORANGE, YELLOW, GREEN, CYAN, BLUE, PURPLE, PINK,
                PURPLE, BLUE, CYAN, GREEN, YELLOW, ORANGE};
        }
    }
    public static final String SKULL
            = "                             ,..oooooooooob..                         \n"
            + "                       ,.dodOOOO\"\"\"\"\"\":\"ooPO88bo..                    \n"
            + "                     .o8O\"\"\" '            \"'\"\"\"PO8b.                  \n"
            + "                 .dd8P'\"                       ''::Y8o.               \n"
            + "               ,d8Po'                             \"':7Ob;             \n"
            + "              d8P::'                                 ';:8b.           \n"
            + "            ;d8''\"                                     ';Y8;          \n"
            + "          ,d8O:'                                        ';:8b.        \n"
            + "         ,88o:'                                           ';Yb.       \n"
            + "        ,8P::'                                           . ';Yb       \n"
            + "       ,8o;:'                                          ,;'  ':8b      \n"
            + "      ,8:::'                                           ;:    :;8b     \n"
            + "     d8o;::                                            o:     ::8,    \n"
            + "    ,8':::                                            :::     :;Y8    \n"
            + "    8'oo:'                                            :::     :::8:   \n"
            + "   dP;:YO                                             ':::;.;;:::Y8.  \n"
            + "  ,8:::;Yb                                            :b::::::::::8b  \n"
            + "  dO;::::8b                                           'Yb::::::::::8. \n"
            + " ,8;:::::O8,                                           'Y88::::::::8: \n"
            + " 8P;::::::88                                             `8O::::::::O \n"
            + " d::::::::88:                                             O8;:::::::8 \n"
            + " 8:::::::888:                                             88b:::::::O:\n"
            + ",8::::::::88:                                            :888Oooo::;Y:\n"
            + "dO:::::::bO8:             ..:.::::::::::...:             :888888P;::db\n"
            + "OP:::::::O88:         ..o8888:::::::::::::)8888bo..       O8888O:::::8\n"
            + "O;::::::::88'    ..od888888888::::\"\"\"\":::88888888888oo;   `8888;:::::8\n"
            + "O:::ob:::;8:  ,d88888888888888::       ':88888888888888b;  '\"88;:::::8\n"
            + "OO::;Yo::OP' d888888888888888O:'      ,.;8888888888888888b  ,88::::::8\n"
            + "YO:::;Y::Ob ,8888888888888888;::       :;88888888888888888  :88::::::8\n"
            + " 8;::::b;8' :8888888888888888o::        ':8888888888888888  :888d::)88\n"
            + " Y:::::88P   888888888888888888'         'O888888888888888  :88888888P\n"
            + " `b:::;8O    d888888888888888P'          ,8888888888888888  '88888888:\n"
            + "  Y::::8:   ,88888888888888P:      ..    '8888888888888888   Y8888888:\n"
            + "  8O::;8'   :88888888888888:      d88,   ':Y88888888888888   '8888888:\n"
            + "  'YbooO    :8888888888P:8P:     :8888:    '':Y8888888888P    \"Y888YP \n"
            + "   '888:     8888888P:;'8O:'     :8P88b       '\"O888888P\"       ;:;o' \n"
            + "    `88:     \"oOOo:.::)O:;:      :8:888.        :8b'            :::'  \n"
            + "     88:      '\"\"\"\" ,do;:'      ,88bO88b     ,.o::PO:;.;.      :::'   \n"
            + "     88';           :' `Yb      d88O`888b    O8\"::::o::::::::::o:;    \n"
            + "     8O::b         ;:   ''     d88Po O888    :8;:\"\"\" '\";88::::o:::    \n"
            + "     YO:;Yb    :o.;::          O8P.: O888     'Y::    :YO8b:::::O'    \n"
            + "      Y:::8b  o;O:::;.         OO;:: OO;'       ''\"  .;bO88\"d:od'     \n"
            + "      `b::;8: :8bo::::;.       OO::: OK:          ,;:8888P',8OP\"      \n"
            + "       Yb:;OO  O888b::::;.     'O:O\",;\"'          ;o8888P  :O\"        \n"
            + "       '`Y888. :88888::::::     ':' db           ;o8888P   8P         \n"
            + "          '`8;  O8888::::::        '8'          :o8888P   :8'         \n"
            + "            OO: :8888::::::         Y:         ,::;888'   :8          \n"
            + "            `8,  o888\"::::'          '         ;:::88:    OP          \n"
            + "            ,8:   O888O8POYOOO\"OPOOPYO8OO8OO888888888'    O:          \n"
            + "            88:   '888o::o':Y: d  O  'Y:'8: O\"Y:`K:8o;    8'          \n"
            + "           88O;    ;88o;:::: : :  :   '  `:   '  ,:8 :   :8           \n"
            + "           O8O:.  ,:OP\"8bd;':: d...      ,.  .db.'\"8 ::  :O           \n"
            + "          ,88O::. ;:O: O\"'\"YP\"YPYP'YO\"\"\"`8K`O\"O `b:O.:;  :O           \n"
            + "          888o:::.:;Ob : :::: :::: :P ,  OO : :  O;:::: ;:O           \n"
            + "          888O::::::::`dbd::b.d::: :: db ::,d.8o;O;::::::dP           \n"
            + "           888::::::::;:\"\"\"\"\"Y8888od8d88o8P\"\"'\"  ':::::'d'            \n"
            + "           \"Y88Odo:\"::::         `\"\"\"\"'           ':::)P'             \n"
            + "             \"\"88888O:::                          ;::dP               \n"
            + "                '\"\"88O:::                   oo;  ;O88'                \n"
            + "                   `Y8O::.  ,.      .      '8b::O88'                  \n"
            + "                     Y8b::. ,)O   ,;'        ::O88'                   \n"
            + "                      '88d:..:   ,d:        ;'d88'                    \n"
            + "                       'Y88d::;.;:8b,   ,..:O88P'                     \n"
            + "                         \"\"88oodO888O::::bd88P'                       \n"
            + "                           '`8888888888888P\"'                         \n"
            + "                                 \"\"\"\"\"\"\"\"";

}
