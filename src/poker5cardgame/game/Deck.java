package poker5cardgame.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Deck {

    private List<Card> deck;

    public Deck() {
        this.deck = new ArrayList<>();
    }

    /**
     * Generate a new deck.
     *
     * @return Deck
     */
    public Deck generate() {
        deck.clear();

        // generate the deck
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                this.deck.add(new Card(suit, rank));
            }
        }
        // shuffle the deck
        Collections.shuffle(deck);
        return this;
    }

    /**
     * Draw a card from the deck.
     * @return Card
     * @throws poker5cardgame.game.Deck.EmptyDeckException
     */
    // TODO test it
    public Card draw() throws EmptyDeckException {
        if (deck.size() > 0) {
            return this.deck.remove(deck.size() - 1);
        }
        //System.err.println("Deck is empty.");
        throw new EmptyDeckException("Deck is empty.");
    }

    /**
     * Get the size of the deck.
     * @return int
     */
    public int getSize() {
        return this.deck.size();
    }
    
    public class EmptyDeckException extends Exception {
        public EmptyDeckException() {
            super();
        }

        public EmptyDeckException(String message) {
            super(message);
        }

        public EmptyDeckException(String message, Throwable cause) {
            super(message, cause);
        }

        public EmptyDeckException(Throwable cause) {
            super(cause);
        }
    }
}
