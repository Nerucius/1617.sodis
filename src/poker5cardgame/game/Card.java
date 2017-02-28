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
        TWO("2", 5),
        THREE("3", 7),
        FOUR("4", 11),
        FIVE("5", 13),
        SIX("6", 17),
        SEVEN("7", 19),
        EIGHT("8", 23),
        NINE("9", 29),
        TEN("10", 31),
        JACK("J", 37),
        QUEEN("Q", 41),
        KING("K", 43),
        ACE("A", 47);

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
        
        public static List<Integer> getSuccessiveValues()
        {
            List<Integer> result = new ArrayList<>();
            int numOfPossibilities = Rank.values().length + 1 - 5; // 9
            for(int i = 0; i < numOfPossibilities; i++)
            {
                int product = 1;
                for(int j = i; j < i + 5; j++)
                    product *= Rank.values()[j].getValue();
                result.add(product);
            }
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

}
