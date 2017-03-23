package poker5cardgame.game;

// TODO @sonia: Maybe this class should've been for a single player, instead of
// for both client and server.
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

    public GameData() {
        //this.deck = new Deck();
        this.sHand = new Hand();
        this.cHand = new Hand();
    }

    @Override
    public String toString() {
        return "GameData{" + "sHand=" + sHand + ", cHand=" + cHand + ", sChips=" + sChips + ", cChips=" + cChips + ", minBet=" + minBet + ", sBet=" + sBet + ", cBet=" + cBet + ", sDrawn=" + sDrawn + ", cDrawn=" + cDrawn + '}';
    }

    public void save(Move move) {
        // TODO refine this method, will be used by AI        

        switch (move.action) {
            case START:
                break;

            case ANTE_STAKES:
                this.minBet = move.chips;
                this.cChips = move.cStakes;
                this.sChips = move.sStakes;
                break;

            case STAKES:
                this.cChips = move.cStakes;
                this.sChips = move.sStakes;
                break;

            case DEALER_HAND:
                this.cHand = new Hand(move.cards);
                break;

            case BET:
                this.cBet = move.chips;
                break;

            case RAISE:
                this.cBet = move.chips;
                break;

            case DRAW:
                this.cHand.discard(move.cards);
                break;
            case DRAW_SERVER:
                if (move.cards != null && move.cards.length > 0)
                    this.cHand.putCards(move.cards);
                break;
            case SHOW:
                break;

        }
    }

}
