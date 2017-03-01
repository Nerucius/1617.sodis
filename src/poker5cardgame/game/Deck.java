package poker5cardgame.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Deck {

    private final List<Card> deck;

    public Deck() {
        this.deck = new ArrayList<>();
        // this.generate();
    }

    public void generate() {
        deck.clear();
        
        // generate the deck
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                this.deck.add(new Card(suit, rank));
            }
        }
        // shuffle the deck
        Collections.shuffle(deck);
    }

    public Card draw() {
        if (deck.size() > 0)
            return this.deck.remove(deck.size()-1);
        System.err.println("Deck is empty.");
        return null;
    }

}
