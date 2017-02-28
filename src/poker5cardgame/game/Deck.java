package poker5cardgame.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {

    // TODO @sonia Create Deck class:
    private List<Card> deck;

    public Deck() {
        this.deck = new ArrayList<>();
        this.generate();
    }

    public void generate() {
        // generate the deck
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                this.deck.add(new Card(suit, rank));
            }
        }
        // shuffle the deck
        Collections.shuffle(deck);
    }
    // TODO delete? Only for testing?
    public int getSize() {
        return this.deck.size();
    }

    // TODO ASK: Should I check if there are more cards like that? Maybe an isEmpty() method? Maybe create a MyExceptions class?
    public Card draw() throws Exception {
        try {
            return this.deck.remove(0);
        } catch (Exception e) {
            throw new Exception("ERROR: Can not draw a card. The deck is empty.");
        }
    }

    @Override
    public String toString() {
        return this.deck.toString();
    }

}
