package poker5cardgame.network;

import poker5cardgame.game.Game;

/**
 * Network definition class. Packet utilities and definitions.
 */
public class Network {

    /** 
     * Possible network packets that can be received by either the server
     * or the client.
     */
    public enum Command {
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
        SHOWDOWN("SHOW"),
        ERROR("ERRO");
        

        public String code;

        private Command(String code) {
            this.code = code;
        }
        
        protected boolean isPacket(String command){
            return command.equalsIgnoreCase(this.code);
        }
        
        static public Command identifyPacket(String code){
            for (Command p : Command.values())
                if (p.isPacket(code))
                    return p;
            
            throw new IllegalArgumentException("Unidentified Packet");
        }
        
    }
    
    public static final class Packet{
        public Command command;
        public String[] args;
        
        public Packet(Command com, String[] args){
            this.command = com;
            this.args = args;
        }
        
    }

}
