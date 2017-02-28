package poker5cardgame.game;

public class HandRanker {

    public enum HandRank implements Comparable<HandRank> {
        NONE,
        HIGH_CARD,
        ONE_PAIR,
        TWO_PAIR,
        TREE_OF_A_KIND,
        STRAIGHT,
        FLUSH,
        FULL_HOUSE,
        FOUR_OF_A_KIND,
        STRAIGHT_FLUSH;
        
        public boolean wins(HandRank other) {
            return (this.compareTo(other) > 0);
        }

        public boolean loses(HandRank otherR) {
            return otherR.wins(this);
        }
    }

    public static HandRank getHandRank(Hand hand) {
        // TODO @sonia Implement rank calculation for a 5 - card Hand
        
        hand.sort();
        
        return HandRank.NONE;

    }

}
