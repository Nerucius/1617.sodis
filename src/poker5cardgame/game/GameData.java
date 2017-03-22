package poker5cardgame.game;

public class GameData {
    
    public static final int MODE_MANUAL = 0;
    public static final int MODE_RANDOM_AI = 1;
    public static final int MODE_INTELLIGENT_AI = 2;
    
    // Server data
    public Deck deck;
    public Hand sHand;
    public int sChips = 10000;
    public int minBet = 100;
    public int sBet = 0;
    public int sDrawn;
    public int sInteractive = MODE_RANDOM_AI; 
    //public int sInteractive = MODE_INTELLIGENT_AI; 
    
    // Client data
    public Hand cHand;
    public int cChips = 1000;
    public int cBet = 0;
    public int cDrawn;
    //public int cInteractive = MODE_MANUAL;
    public int cInteractive = MODE_RANDOM_AI;
    
    public GameData()
    {
      //this.deck = new Deck();
      this.sHand = new Hand();
      this.cHand = new Hand();
    }
    
    @Override
    public String toString() {
        return "GameData{" + "sHand=" + sHand + ", cHand=" + cHand + ", sChips=" + sChips + ", cChips=" + cChips + ", minBet=" + minBet + ", sBet=" + sBet + ", cBet=" + cBet + ", sDrawn=" + sDrawn + ", cDrawn=" + cDrawn + '}';
    }
   
}
