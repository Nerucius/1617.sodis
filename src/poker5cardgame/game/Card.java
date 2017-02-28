package poker5cardgame.game;

public class Card implements Comparable<Card> {

    // TODO @sonia IMPLEMENT CARD CLASS, with enums or static finals
    // Cards have codes and values.
    public enum Suit {
        CLUBS("C"),
        DIAMONDS("D"),
        HEARTS("H"),
        SPADES("S");

        private final String code;

        Suit(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    public enum Rank {
        TWO("2", 1),
        THREE("3", 2),
        FOUR("4", 3),
        FIVE("5", 4),
        SIX("6", 5),
        SEVEN("7", 6),
        EIGHT("8", 7),
        NINE("9", 8),
        TEN("10", 9),
        JACK("J", 10),
        QUEEN("Q", 11),
        KING("K", 12),
        ACE("A", 13);

        private final String code;
        private final int value;

        Rank(String code, int value) {
            this.code = code;
            this.value = value;
        }

        public String getCode() {
            return code;
        }

        public int getValue() {
            return value;
        }
    }

    private final Suit suit;
    private final Rank rank;

    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public Suit getSuit() {
        return this.suit;
    }

    public Rank getRank() {
        return this.rank;
    }

    public String getCode() {
        return this.rank.code + this.suit.code;
    }

    public int getValue() {
        return this.rank.value;
    }

    @Override
    public int compareTo(Card other) {
        // TODO ASK: Is this card ordering enough?
        return - this.rank.compareTo(other.rank);
    }

    @Override
    public String toString() {
        return rank.code + suit.code;
    }

}
