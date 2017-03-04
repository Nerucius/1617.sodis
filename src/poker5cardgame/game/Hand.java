package poker5cardgame.game;

import java.util.ArrayList;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import poker5cardgame.game.Card.Rank;
import poker5cardgame.game.HandRanker.HandRank;
import static poker5cardgame.game.HandRanker.getHandRank;

public class Hand {

    // <editor-fold defaultstate="collapsed" desc="Data Definition">
    private final int SIZE = 5;
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Attributes">
    private final List<Card> cards; // List of the card that contains a hand
    private List<Card> auxCards;    // To find the high card in the hand ranker
    private int suitId;             // Id of the hand to manage the case FLUSH in the hand ranker
    private int weight;             // Weight of the hand to manage the hand ranker
    private Map dict;               // Map that contains the required information to manage the hand ranker
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructors">
    public Hand() {
        this.cards = new ArrayList();
        this.auxCards = new ArrayList();
    }

    public Hand(Deck deck) {
        this.cards = new ArrayList();
        this.auxCards = new ArrayList();

        this.generate(deck);
        //this.computeHandValue();
        this.generateRankerInformation();
    }
    //</editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Getters">
    public List<Card> getCards() {
        return this.cards;
    }

    public int getSuitId() {
        return suitId;
    }
 
    public int getWeight() {
        return weight;
    }
    
    public Map getDict() {
        return dict;
    }
   
        // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Public Methods">
    /**
     * Generate a new hand (5 cards) from the deck.
     * 
     * @param deck Deck that contains all the remaining cards
     */
    public void generate(Deck deck) {
        for (int i = 0; i < SIZE; i++) {
            putCard(deck.draw());
        }
    }

    /**
     * Put a new card to the hand.
     *
     * @param card Card to add to the hand
     */
    public void putCard(Card card) {
        if (cards.size() < SIZE) {
            cards.add(card);
        } else {
            System.err.println("Too many cards added to the hand.");
        }
    }

    /**
     * Remove one or more cards from the hand.
     *
     * @param cards Card or Cards to remove
     */
    public void discard(Card... cards) {
        for (Card c : cards) {
            if (!this.cards.remove(c)) {
                System.err.println("Tried to remove a non existing card.");
            }
        }
    }
    
    @Override
    public String toString() {
        return this.cards.toString();
    }
    // </editor-fold>

    private void resetAuxCards()
    {
        this.auxCards.clear();
        this.auxCards.addAll(cards);
        Collections.sort(auxCards);
        Collections.reverse(auxCards); // We want first the highest card
    }
    /**
     * @param h Other Hand to compare to
     * @return 1 if Win, 0 if tie, -1 if loses.
     */
    // TODO change because HandRank comparasisons have been changed
    public int compareTo2(Hand h){
        HandRank ra = HandRanker.getHandRank(this);
        HandRank rb = HandRanker.getHandRank(h);
        if (ra.wins(rb))
            return 1;
        else if(ra.loses(rb))
            return -1;
        
        // TODO This is wrong, needs to first look into the Rank's card value.
        Card highestA = this.getHighCard();
        Card highestB = this.getHighCard();
        return highestA.compareTo(highestB);
    }
    
    
    
    
    public Card getCard(int i) {
        return this.cards.get(i);
    }
    
    
    
    // TODO ponderator = 0? (count)
    /*public void computeHandValue()
    {
        int product = 1;
        List<Rank> values = this.getRanks();

        for (Card card : this.cards) {
            if (values.isEmpty())
                break;

            int ponderator = 0;
            while (values.contains(card.getRank())) {
                ponderator++;
                values.remove(card.getRank());
            }

            if (ponderator != 0)
                product *= ponderator * card.getRankWeight();
        }
        this.weight = product;
    }*/
    
    /*public int getSuitValue() {
        int product = 1;
        for(Card card : cards)
            product *= card.getSuitId();
        return product;
    }*/
    
    /*public List<Rank> getRanks() {
        List<Rank> ranks = new ArrayList<>();
        for(Card c  : cards)
            ranks.add(c.getRank());
        return ranks;
    }*/
    
    public void generateRankerInformation()
    {
        this.dict = new HashMap();
        this.weight = 1;
        this.suitId = 1;
        
        for(Card card : this.cards)
        {
            // generate the map with elements <key = rank, value = rank occurrences>
            if (!this.dict.containsKey(card.getRank()))
                this.dict.put(card.getRank(), 1);
            else
                this.dict.replace(card.getRank(), (int) this.dict.get(card.getRank()) + 1);
            
            // compute the product of the weight prime numbers of the cards
            this.weight *= card.getRankWeight();
            
            // compute the product of the suit ids of the cards
            this.suitId *= card.getSuitId();
        }
    }

    

    

    private Card getHighCard() {
        if(auxCards.isEmpty())
            return null;
        return (Card) auxCards.remove(0);
    }
    
    private int caseHighCard(Hand other)
    {
        Card mCard, oCard;
        do
        {
            mCard = this.getHighCard();
            oCard = other.getHighCard();
            
        } while (mCard != null && mCard.getRank().equals(oCard.getRank()));
        if(mCard == null)
            return 0;
        return mCard.compareTo(oCard);
    }
    
    private int caseOnePair(Hand other)
    {
        return 0;
    }
    // TODO order winner functions, delete unnecessary things and put it to the right class: this method should be in Hand class
    public int compareTo(Hand other) {
        HandRank mHandRank = getHandRank(this);
        HandRank oHandRank = getHandRank(other);

        if (mHandRank.wins(oHandRank))
            return 1;
        else if (mHandRank.loses(oHandRank))
            return -1;
        else {
            // Both hand have the same HandRank. We need to compare the cards.
       
            switch(mHandRank)
            {
                case HIGH_CARD:
                    this.resetAuxCards();
                    return caseHighCard(other);
                case ONE_PAIR:
                    break;
                case TWO_PAIR:
                    break;
                case THREE_OF_A_KIND:
                    break;
                case STRAIGHT:
                    break;
                case FLUSH:
                    break;
                case FULL_HOUSE:
                    break;
                case FOUR_OF_A_KIND:
                    break;
                case STRAIGHT_FLUSH:
                    
                    break;
                default:
                    break;
            }
        }
        return -1;
    }

}
