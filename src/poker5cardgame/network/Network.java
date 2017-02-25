package poker5cardgame.network;

/**
 * Network definition class. Packet utilities and definitions.
 */
public class Network {

    /** 
     * Possible network packets that can be received by either the server
     * or the client.
     */
    public enum Packet {
        /** Sent by Client: To start a Game */
        START("STRT"),
        ANTE("ANTE"),
        STAKES("STKS"),
        /** Sent by Client: To accept the server ANTE message */
        ANTE_OK("ANOK"),
        /** Sent by Client: To quit the game, after server ANTE */
        QUIT("QUIT"),
        DEALER("DEAL"),
        HAND("HAND"),
        /** Sent by Client: To pass on BETtting */
        PASS("PASS"),
        /** Sent by Client: To BET a new amount of money */
        BET("BET_"),
        CALL("CALL"),
        FOLD("FOLD"),
        DRAW("DRAW"),
        DRAW_SERVER("DRWS"),
        SHOWDOWN("SHOW");

        private String code;
        /** Argument list for the packet */
        public String[] args = null;

        private Packet(String code) {
            this.code = code;
        }
        
        protected boolean isPacket(String command){
            return command.equalsIgnoreCase(this.code);
        }
        
        static public Packet identifyPacket(String str){
            for (Packet p : Packet.values())
                if (p.isPacket(str))
                    return p;
            
            throw new IllegalArgumentException("Unidentified Packet");
        }
        
    }

}
