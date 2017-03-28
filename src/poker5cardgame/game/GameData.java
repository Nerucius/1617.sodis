package poker5cardgame.game;

public class GameData {


    // Common data (0:server;1:client);
    public int dealer = -1;
    public int winner = -1; //(2;tie)
    
    // Server data
    public Deck deck;
    public Hand sHand;
    public int sChips;
    public int initialBet;
    public int sBet = 0;
    public int sDrawn = -1;
    public int sInteractive = 1; 

    // Client data
    public int cId = -1;
    public Hand cHand;
    public int cChips;
    public int cBet = 0;
    public int cDrawn = -1;
    public int cInteractive = 0;

    public GameData() {
        this.sHand = new Hand();
        this.cHand = new Hand();
    }

    @Override
    public String toString() {
        return "GameData{" + "dealer=" + dealer + ", winner=" + winner + ", sHand=" + sHand + ", sChips=" + sChips + ", initialBet=" + initialBet + ", sBet=" + sBet + ", sDrawn=" + sDrawn + ", cId=" + cId + ", cHand=" + cHand + ", cChips=" + cChips + ", cBet=" + cBet + ", cDrawn=" + cDrawn + '}';
    }

    public void save(Move move, boolean server) {
        
        switch (move.action) {
            
            case NOOP:
                break;
                
            case START:
                this.cId = move.id;
                break;

            case ANTE_STAKES:
                this.initialBet = move.chips;
                this.cChips = move.cStakes;
                this.sChips = move.sStakes;
                break;

            case ANTE_OK:
                this.winner = -1;
                this.cChips -= this.initialBet;
                this.sChips -= this.initialBet;
                this.cBet = this.initialBet;
                this.sBet = this.initialBet;
                break;

            case DEALER_HAND:
                this.dealer = move.dealer;
                this.cHand = new Hand(move.cards);
                break;

            case BET:
                if(server)
                {
                    this.sBet += move.chips;
                    this.sChips -= move.chips;
                }
                else
                {
                    this.cBet += move.chips;
                    this.cChips -= move.chips;
                }
                break;

            case RAISE:
                if(server)
                {
                    int amount = this.cBet - this.sBet + move.chips;
                    this.sBet += amount;
                    this.sChips -= amount;
                }
                else
                {
                    int amount = this.sBet - this.cBet + move.chips;
                    this.cBet += amount;
                    this.cChips -= amount;
                }
                break;
                
            case CALL:
                if(server)
                {
                    int amount = this.cBet - this.sBet;
                    this.sBet += amount;
                    this.sChips -= amount;
                }
                else
                {
                    int amount = this.sBet - this.cBet;
                    this.cBet += amount;
                    this.cChips -= amount;
                }
                break;
                
            case FOLD:
                if(server)                
                    this.cChips += this.sBet + this.cBet;                
                else
                    this.sChips += this.sBet + this.cBet;                
                break;

            case DRAW:
                this.cDrawn = move.cDrawn;
                try {
                    this.cHand.discard(move.cards);
                } catch (Exception ex) {/* Ignored. This exception does not reach this point */}
                break;
                
            case DRAW_SERVER:
                if (this.cDrawn > 0)
                    this.cHand.putCards(move.cards);
                this.sDrawn = move.sDrawn;
                break;

            case SHOW:
                this.sHand = new Hand(move.cards);
                
                this.winner = move.winner;
                if(this.winner == 0)                
                    this.sChips += this.sBet + this.cBet;                
                if(this.winner == 1)
                    this.cChips += this.sBet + this.cBet; 
                else if(this.winner == 2)
                {
                    this.cChips += this.cBet;
                    this.sChips += this.sBet;
                }
                                
                if (this.winner != -1) {
                    // reset round data
                    this.cDrawn = -1;
                    this.sDrawn = -1;
                    this.cBet = 0;
                    this.sBet = 0;
                }
                break;
                
            case STAKES:
                this.cChips = move.cStakes;
                this.sChips = move.sStakes;
                break;
        }
    }
}
