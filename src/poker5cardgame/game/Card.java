package poker5cardgame.game;

import java.util.ArrayList;
import java.util.List;

public class Card implements Comparable<Card> {

    public enum Suit {
        CLUBS("C", 2),
        DIAMONDS("D", 3),
        HEARTS("H", 7),
        SPADES("S", 11);

        private final String code;
        private final int value;

        Suit(String code, int value) {
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

    public enum Rank {
        TWO("2", 11),
        THREE("3", 13),
        FOUR("4", 17),
        FIVE("5", 19),
        SIX("6", 23),
        SEVEN("7", 29),
        EIGHT("8", 31),
        NINE("9", 37),
        TEN("10", 41),
        JACK("J", 43),
        QUEEN("Q", 47),
        KING("K", 53),
        ACE("A", 59);

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

        public static List<Integer> getSuccessiveValues() {
            // TODO @sonia extreure llista estatica a fora i afegir as davant
            List<Integer> result = new ArrayList<>();
            int numOfPossibilities = Rank.values().length + 1 - 5; // 9
            for (int i = 0; i < numOfPossibilities; i++) {
                int product = 1;
                for (int j = i; j < i + 5; j++)
                {
                    product *= Rank.values()[j].getValue();
                }
                result.add(product);
            }      
            // Special case: A 1 2 3 4
            int product = Rank.ACE.getValue();
            for(int i = 0; i < 4; i++)
            {
                product *= Rank.values()[i].getValue();
            }
            result.add(product);
            return result;
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

    /** Convenience method for converting card codes to real cards
     * @return Card object
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

    public int getValue() {
        return this.getRank().getValue();
    }

    public int getSuitValue() {
        return this.getSuit().getValue();
    }

    @Override
    public int compareTo(Card other) {
        // TODO ASK: Is this card ordering enough?
        return -this.getRank().compareTo(other.getRank());
    }

    @Override
    public String toString() {
        return this.getCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Card))
            return false;
        Card other = (Card)obj;
        return this.rank == other.rank && this.suit == other.suit;        
    }
    
    

}
