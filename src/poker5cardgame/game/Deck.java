package poker5cardgame.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Deck {

    // <editor-fold defaultstate="collapsed" desc="Attributes">
    private List<Card> deck;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructors">
    public Deck() {
        this.deck = new ArrayList<>();
        // this.generate();
        // ---- ASK @alex: why is this commented?
        // TODO REPLY: Because it's better to call generate when you need it.
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Public Methods">
    /**
     * Generate a new deck.
     */
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

    /**
     * Draw a card from the deck.
     *
     * @return Card
     */
    public Card draw() {
        if (deck.size() > 0) {
            return this.deck.remove(deck.size() - 1);
        }
        System.err.println("Deck is empty.");
        return null;
    }
    // </editor-fold>
}
