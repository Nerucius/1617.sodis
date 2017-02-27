package poker5cardgame.game;

public class Card implements Comparable<Card>{
    
    // TODO @sonia IMPLEMENT CARD CLASS, with enums or static finals
    // Cards have codes and values.
    
    public static final int ACE = 13;
    
    public static final String DIAMONDS = "D";
    
    public enum Suit{
        DIAMONDS("D");
        
        private String code;
        private int value;
        private Suit(String code){
            this.code = code;
        }
        
        @Override
        public String toString(){
            return "";
        }
    }
    
    @Override
    public int compareTo(Card o) {
        // TODO Define card ordering here
        String cardid = ACE+DIAMONDS;
        return 0;       
    }
    
}
