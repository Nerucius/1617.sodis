package poker5cardgame.game;

import poker5cardgame.game.Card.Rank;

public class HandRanker {

    public enum HandRank implements Comparable<HandRank> {
        HIGH_CARD,
        ONE_PAIR,
        TWO_PAIR,
        THREE_OF_A_KIND,
        STRAIGHT,
        FLUSH,
        FULL_HOUSE,
        FOUR_OF_A_KIND,
        STRAIGHT_FLUSH;

        // TODO ASK: should we manage a tie case?
        public boolean wins(HandRank other) {                
            return (this.compareTo(other) > 0);
        }

        public boolean loses(HandRank otherR) {
            return otherR.wins(this);
        }
    }

    public static HandRank getHandRank(Hand hand) {
        int x = hand.getValue();
        boolean areSuccessive = areSuccessive(x);
        boolean areSameSuit = areSameSuit(hand);

        // Check if the cards are successive => STRAIGHT_FLUSH, STRAIGHT
        if (areSuccessive) {
            // Check if the cards are of the same suit => STRAIGHT_FLUSH
            if (areSameSuit) {
                return HandRank.STRAIGHT_FLUSH;
            }
            return HandRank.STRAIGHT;
        }
        // Case 4 | x => FOUR_OF_A_KIND, TWO_PAIR
        if (x % 4 == 0) {
            hand.sort();
            if (x / (4 * hand.getCard(0).getValue() * hand.getCard(4).getValue()) == 1) {
                return HandRank.FOUR_OF_A_KIND;
            }
            // Check if the cards are of the same suit (ifnot) => TWO_PAIR
            if (!areSameSuit) {
                return HandRank.TWO_PAIR;
            }
        }
        // Case 3 | x => FULL_HOUSE, THREE_OF_A_KIND
        if (x % 3 == 0) {
            // Check if 2 | x => FULL_HOUSE
            if (x % 2 == 0) {
                return HandRank.FULL_HOUSE;
            }
            // Check if the cards are of the same suit (ifnot) => THREE_OF_A_KIND
            if (!areSameSuit) {
                return HandRank.THREE_OF_A_KIND;
            }
        }
        // Check if the cards are of the same suit => FLUSH
        if (areSameSuit) {
            return HandRank.FLUSH;
        }
        // Case 2 | x => ONE_PAIR
        if (x % 2 == 0) {
            return HandRank.ONE_PAIR;
        }
        return HandRank.HIGH_CARD;
    }

    private static boolean areSuccessive(int x) {
        return Rank.getSuccessiveValues().contains(x);
    }

    private static boolean areSameSuit(Hand hand) {
        return Math.pow(hand.getSuitValue(), 1. / 5) % 1 == 0;
    }
}
