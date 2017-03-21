package poker5cardgame.game;

public class GameData {
    
    /* Game resources */
    public boolean newPlay = true;
    public Deck deck;
    public Hand sHand, cHand;
    public int sChips = 10000, cChips = 1000;
    public int minBet = 100;
    public int sBet = 0, cBet = 0;
    public int sDrawn, cDrawn;
    
    /* Game flags */
    // sInteractive to know which way plays the server (1: auto without IA; 2: auto with IA)
    // cInteractive to know which wat plays the client (0: manual; 1: auto without IA; 2: auto with IA)
    public int sInteractive = 1, cInteractive = 0;

    public GameData()
    {
      this.deck = new Deck();
      this.sHand = new Hand();
      this.cHand = new Hand();
    }

    @Override
    public String toString() {
        return "GameData{" + "newPlay=" + newPlay + ", deck=" + deck.getSize() + ", sHand=" + sHand + ", cHand=" + cHand + ", sChips=" + sChips + ", cChips=" + cChips + ", minBet=" + minBet + ", sBet=" + sBet + ", cBet=" + cBet + ", sDrawn=" + sDrawn + ", cDrawn=" + cDrawn + '}';
    }

        
}
