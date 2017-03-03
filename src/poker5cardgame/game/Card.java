package poker5cardgame.game;

public class Card implements Comparable<Card> {

    // <editor-fold defaultstate="collapsed" desc="Data definition">
    // Prime numbers to give weigth to each card
    private static final int PRIME_01 = 11; // 2
    private static final int PRIME_02 = 13; // 3
    private static final int PRIME_03 = 17; // 4
    private static final int PRIME_04 = 19; // 5
    private static final int PRIME_05 = 23; // 6
    private static final int PRIME_06 = 29; // 7
    private static final int PRIME_07 = 31; // 8
    private static final int PRIME_08 = 37; // 9
    private static final int PRIME_09 = 41; // 10
    private static final int PRIME_10 = 43; // J
    private static final int PRIME_11 = 47; // Q
    private static final int PRIME_12 = 53; // K
    private static final int PRIME_13 = 59; // A

    // Possible hand ranks when the hand is a STRAIGHT
    public final int[] SUCCESSIVE_CARDS
            = {
                PRIME_13 * PRIME_01 * PRIME_02 * PRIME_03 * PRIME_04, // A 2 3 4 5 (Special case)
                PRIME_01 * PRIME_02 * PRIME_03 * PRIME_04 * PRIME_05, // 2 3 4 5 6
                PRIME_02 * PRIME_03 * PRIME_04 * PRIME_05 * PRIME_06, // 3 4 5 6 7
                PRIME_03 * PRIME_04 * PRIME_05 * PRIME_06 * PRIME_07, // 4 5 6 7 8
                PRIME_04 * PRIME_05 * PRIME_06 * PRIME_07 * PRIME_08, // 5 6 7 8 9
                PRIME_05 * PRIME_06 * PRIME_07 * PRIME_08 * PRIME_09, // 6 7 8 9 10
                PRIME_06 * PRIME_07 * PRIME_08 * PRIME_09 * PRIME_10, // 7 8 9 10 J
                PRIME_07 * PRIME_08 * PRIME_09 * PRIME_10 * PRIME_11, // 8 9 10 J Q
                PRIME_08 * PRIME_09 * PRIME_10 * PRIME_11 * PRIME_12, // 9 10 J Q K
                PRIME_09 * PRIME_10 * PRIME_11 * PRIME_12 * PRIME_13 // 10 J Q K A
            };

    // Possible suits for a card
    public enum Suit {
        CLUBS("C", PRIME_01),
        DIAMONDS("D", PRIME_02),
        HEARTS("H", PRIME_03),
        SPADES("S", PRIME_04);

        private String code;    // suit code to write and read the card
        private int id;         // id to find easily a FLUSH case

        Suit(String code, int id) {
            this.code = code;
            this.id = id;
        }
    }

    // Possible ranks for a card
    public enum Rank {
        TWO("2", PRIME_01),
        THREE("3", PRIME_02),
        FOUR("4", PRIME_03),
        FIVE("5", PRIME_04),
        SIX("6", PRIME_05),
        SEVEN("7", PRIME_06),
        EIGHT("8", PRIME_07),
        NINE("9", PRIME_08),
        TEN("10", PRIME_09),
        JACK("J", PRIME_10),
        QUEEN("Q", PRIME_11),
        KING("K", PRIME_12),
        ACE("A", PRIME_13);

        private String code;    // rank code to write and read the card
        private int weight;     // weight to manage the hand ranker

        Rank(String code, int value) {
            this.code = code;
            this.weight = value;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Attributes">
    private Suit suit;
    private Rank rank;
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructors">
    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Public Methods">
    public int getRankWeight() {
        return this.rank.weight;
    }

    public int getSuitId() {
        return this.suit.id;
    }
    
    /**
     * Get the card code from a real card.
     * 
     * @return String
     */
    public String getCode() {
        return this.rank.code + this.suit.code;
    }

    /**
     * Get the real card from a card code.
     *
     * @return Card
     */
    public static Card fromCode(String code) {
        Card c;

        // Special case for rank 10, thanks Eloi
        if (code.charAt(0) == 1) {
            String suit = "" + code.charAt(2);
            c = new Card(Suit.valueOf(suit), Rank.TEN);
            
        } else {
            Rank rank = Rank.valueOf("" + code.charAt(0));
            Suit suit = Suit.valueOf("" + code.charAt(1));
            c = new Card(suit, rank);
        }
        return c;
    }

    @Override
    public int compareTo(Card other) {
        // TODO ASK: Is this card ordering enough?
        return -this.rank.compareTo(other.rank);
    }

    @Override
    public String toString() {
        return this.getCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Card)) {
            return false;
        }
        Card other = (Card) obj;
        return this.rank == other.rank && this.suit == other.suit;
    }
    // </editor-fold>
}
