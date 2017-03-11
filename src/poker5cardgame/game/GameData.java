package poker5cardgame.game;

public class GameData {
    
    // Game resources
    private Deck deck;
    private Hand sHand, cHand;
    private int sChips = 10000, cChips = 1000;
    private int anteBet = 100;
    private int sBet, cBet;
    
    // Game flags
    protected Game.State state = Game.State.INIT;
    private boolean secondRound = false;
    private boolean serverTurn;

    public GameData()
    {
      this.deck = new Deck();
      this.sHand = new Hand();
      this.cHand = new Hand();
    }

    public Deck getDeck() {
        return deck;
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    public Hand getsHand() {
        return sHand;
    }

    public void setsHand(Hand sHand) {
        this.sHand = sHand;
    }

    public Hand getcHand() {
        return cHand;
    }

    public void setcHand(Hand cHand) {
        this.cHand = cHand;
    }

    public int getsChips() {
        return sChips;
    }

    public void setsChips(int sChips) {
        this.sChips = sChips;
    }

    public int getcChips() {
        return cChips;
    }

    public void setcChips(int cChips) {
        this.cChips = cChips;
    }

    public int getAnteBet() {
        return anteBet;
    }

    public void setAnteBet(int anteBet) {
        this.anteBet = anteBet;
    }

    public int getsBet() {
        return sBet;
    }

    public void setsBet(int sBet) {
        this.sBet = sBet;
    }

    public int getcBet() {
        return cBet;
    }

    public void setcBet(int cBet) {
        this.cBet = cBet;
    }

    public Game.State getState() {
        return state;
    }

    public void setState(Game.State state) {
        this.state = state;
    }

    public boolean isSecondRound() {
        return secondRound;
    }

    public void setSecondRound(boolean secondRound) {
        this.secondRound = secondRound;
    }

    public boolean isServerTurn() {
        return serverTurn;
    }

    public void setServerTurn(boolean serverTurn) {
        this.serverTurn = serverTurn;
    }    
}
