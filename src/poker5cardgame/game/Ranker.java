/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker5cardgame.game;

/**
 *
 * @author gdempegu11.alumnes
 */
public class Ranker {

    public enum Rank implements Comparable<Rank> {
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

        public boolean wins(Rank other) {
            return (this.compareTo(other) > 0);
        }

        public boolean loses(Rank otherR) {
            return otherR.wins(this);
        }
    }

    public static Rank getRank(Hand hand) {
        // TODO Implement rank calculation for a 5 - card Hand
        return Rank.NONE;

    }

}
