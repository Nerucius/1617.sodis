/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker5cardgame.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author gdempegu11.alumnes
 */
public class Hand {

    // TODO @alex define methods
    private List<Card> hand;

    public Hand() {
        this.hand = new ArrayList();
    }

    // TODO message for @alex: He implementat aquest metode per testejar el HandRanker
    public void draw5FromDeck(Deck deck) throws Exception {
        for (int i = 0; i < 5; i++) {
            hand.add(deck.draw());
        }
    }

    public void putCard(Card c) {
        // TODO Check max of cards
    }

    public void discard(Card... cards) {
        // TODO Check if hand has cards
    }

    public void sort() {
        Collections.sort(hand);
    }

    @Override
    public String toString() {
        return this.hand.toString();
    }

}
