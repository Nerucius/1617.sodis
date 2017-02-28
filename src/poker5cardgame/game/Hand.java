/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker5cardgame.game;

import java.util.ArrayList;

import java.util.Collections;
import java.util.List;
import poker5cardgame.game.Card.Rank;

/**
 *
 * @author gdempegu11.alumnes
 */
public class Hand {

    // TODO @alex define methods
    private final List<Card> hand;

    public Hand() {
        this.hand = new ArrayList();
    }
    
    // TODO DELETE message for @alex: He implementat aquest metode NOMES per TESTEJAR el HandRanker, el bo es el putCard que implementes tu
    public void addCard(Card card)
    {
        this.hand.add(card);
    }
    
    // TODO message for @alex: He implementat aquest metode pel HandRanker
    public List<Card> getCards()
    {
        return this.hand;
    }
    
    // TODO message for @alex: He implementat aquest metode pel HandRanker
    public List<Rank> getValues()
    {
        List<Rank> values = new ArrayList<>();
        this.getCards().stream().forEach((card) -> {
            values.add(card.getRank());
        });
        return values;
    }
    // TODO message for @alex: He implementat aquest metode pel HandRanker
    public Card getCard(int i)
    {
        return this.hand.get(i);
    }
    
    // TODO message for @alex: He implementat aquest metode pel HandRanker
    // TODO think how to do it better ?
    public int getValue() {
        int product = 1;
        List<Rank> values = this.getValues();

        for(Card card : this.getCards())
        {
            if(values.isEmpty())
                break;
            
            int ponderator = 0;
            while(values.contains(card.getRank()))
            {
                ponderator++;
                values.remove(card.getRank());
            }

            if(ponderator != 0)
                product *= ponderator * card.getValue();            
        }
        return product;
    }
    
    // TODO message for @alex: He implementat aquest metode pel HandRanker
    public int getSuitValue() {
        int product = 1;
        product = hand.stream().map((card) -> card.getSuitValue()).reduce(product, (accumulator, _item) -> accumulator * _item);
        return product;
    }
    
    // TODO message for @alex: He implementat aquest metode pel HandRanker
    public void sort()
    {
        Collections.sort(hand);
    }
    
    public void draw5FromDeck(Deck deck) throws Exception {

    }
    
    public void putCard(Card c) {
        // TODO Check max of cards
    }

    public void discard(Card... cards) {
        // TODO Check if hand has cards
    }

    @Override
    public String toString() {
        return this.hand.toString();
    }

}
