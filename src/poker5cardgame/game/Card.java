package poker5cardgame.game;

public class Card implements Comparable<Card> {

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
        TWO("2", 0),
        THREE("3", 1),
        FOUR("4", 2),
        FIVE("5", 3),
        SIX("6", 4),
        SEVEN("7", 5),
        EIGHT("8", 6),
        NINE("9", 7),
        TEN("10", 8),
        JACK("J", 9),
        QUEEN("Q", 10),
        KING("K", 11),
        ACE("A", 12);

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
        return this.getRank().getCode() + this.getSuit().getCode();
    }

    public int getValue() {
        return this.getRank().getValue();
    }

    @Override
    public int compareTo(Card other) {
        // TODO ASK: Is this card ordering enough?
        return - this.getRank().compareTo(other.getRank());
    }

    @Override
    public String toString() {
        return this.getCode();
    }

}
