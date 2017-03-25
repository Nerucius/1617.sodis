package poker5cardgame.game;

// TODO @sonia: Maybe this class should've been for a single player, instead of

import java.util.logging.Level;
import java.util.logging.Logger;

// for both client and server.
public class GameData {


    // Common data (0:server;1:client);
    public int dealer = -1;
    public int winner = -1;
    
    // Server data
    public Deck deck;
    public Hand sHand;
    public int sChips = 10000;
    public int initialBet = 100;
    public int sBet = 0;
    public int sDrawn = -1;
    public int sInteractive = 1; 

    // Client data
    public int cId = -1;
    public Hand cHand;
    public int cChips = 1000;
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

    public void save(Move move, boolean serverTurn) throws Exception {
        // TODO refine this method, will be used by AI        

        switch (move.action) {
            case START:
                this.cId = move.id;
                break;

            case ANTE_STAKES:
                this.initialBet = move.chips;
                this.cChips = move.cStakes;
                this.sChips = move.sStakes;
                break;

            case ANTE_OK:
                // TODO aixo es un manage ?
                this.cChips -= this.initialBet;
                this.sChips -= this.initialBet;
                this.cBet += this.initialBet;
                this.sBet += this.initialBet;
                break;

            case DEALER_HAND:
                this.dealer = move.dealer;
                this.cHand = new Hand(move.cards);
                break;

            case BET:
                System.out.println("SONIA DEBUG: save game data: server turn? " + serverTurn);
                if(serverTurn)
                    this.sBet = move.chips;
                else
                    this.cBet = move.chips;
                break;

            case RAISE:
                if(serverTurn)
                    this.sBet = move.chips;
                else
                    this.cBet = move.chips;
                break;

            case DRAW:
                this.cDrawn = move.cDrawn;
                this.cHand.discard(move.cards);                
                break;
                
            case DRAW_SERVER:
                if (this.cDrawn > 0)
                    this.cHand.putCards(move.cards);
                this.sDrawn = move.sDrawn;
                break;

            case STAKES:
                this.winner = move.winner;
                
                // reset round data
                this.cDrawn = -1;
                this.sDrawn = -1;
                this.cBet = 0;
                this.sBet = 0;
                this.initialBet = 0;
                
                this.cChips = move.cStakes;
                this.sChips = move.sStakes;
                break;
        }
    }
}
