/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker5cardgame.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author gdempegu11.alumnes
 */
public class Ranker {
    
    public enum Rank implements Comparable<Rank>{
        // TODO Finish rank list
        NONE,
        PAIR,
        DOUBLE_PAIR,
        TRIPLE;

        public int compareTo(Comparable<Rank> r){
            // TODO follow compareTo conventions
            return 0;
        }
    }
    
    
    public static Rank getRank(Hand hand){
        // TODO Implement rank calculation for a 5 - card Hand        
        return Rank.NONE;  
        
    }
    
    
}
