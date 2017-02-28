package poker5cardgame.game;

public class HandRanker {

    public enum HandRank implements Comparable<HandRank> {
        NONE,
        PAIR,
        DOUBLE_PAIR,
        TREE_OF_A_KIND,
        STRAIGHT,
        FLUSH,
        FULL_HOUSE,
        FOUR_OF_A_KIND,
        STRAIGHT_FLUSH,
        FIVE_OF_A_KIND;
        
        /* 0 for 2, 13 for Ace */

        public boolean wins(HandRank other) {
            return (this.compareTo(other) > 0);
        }

        public boolean loses(HandRank otherR) {
            return otherR.wins(this);
        }
    }

    public static HandRank getHandRank(Hand hand) {
        // TODO @sonia Implement rank calculation for a 5 - card Hand
        return HandRank.NONE;

    }

}
