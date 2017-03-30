package poker5cardgame.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import static poker5cardgame.Log.*;
import poker5cardgame.game.Card.Rank;
import poker5cardgame.game.HandRanker.HandRank;
import static poker5cardgame.game.HandRanker.getHandRank;
import static poker5cardgame.Log.*;


public class Hand implements Comparable<Hand> {

    public static final int SIZE = 5;

    private final List<Card> cards; // List of the card that contains a hand
    private int suitId;             // Id of the hand to manage the case FLUSH in the hand ranker
    private int weight;             // Weight of the hand to manage the case STRAIGHT in the hand ranker
    private Map ocurDict;           // Map that contains <Rank,Occurrences> elements
    private Map rankDict;           // Map that contains <HandRank, Rank> elements

    public Hand() {
        this.cards = new ArrayList();
    }

    public Hand(Card[] cards) {
        this.cards = new ArrayList<>();
        putCards(cards);
    }

    public List<Card> getCards() {
        return this.cards;
    }

    public int getSize() {
        return this.cards.size();
    }

    public int getSuitId() {
        return suitId;
    }

    public int getWeight() {
        return weight;
    }

    public Map getOcurDict() {
        return ocurDict;
    }

    public Map getRankDict() {
        return rankDict;
    }

    /**
     * Generate a new hand (5 cards) from the deck.
     *
     * @param deck Deck that contains all the remaining cards
     */
    public void draw5FromDeck(Deck deck) {
        this.cards.clear();
        for (int i = 0; i < SIZE; i++) {
            putCards(deck.draw());
        }
    }

    /**
     * Put one or more cards into the hand. Does bound checks.
     *
     * @param newCards Cards to add to the hand
     */
    public void putCards(Card... newCards) {
        for (Card c : newCards) {
            if (this.cards.size() < SIZE) {
                this.cards.add(c);
            } else
                GAME_ERROR(0,"Hand: Too many cards added to the hand. Ignored card");
        }
    }



    /**
     * Put n new cards from the deck to the hand.
     *
     * @param deck Deck that contains all the remaining cards
     * @param n int that is the number of cards to put into the hand
     * @return Card[]
     */
    public Card[] putNCards(Deck deck, int n) {
        Card[] cards = new Card[n];
        for (int i = 0; i < n; i++) {
            Card card = deck.draw();
            putCards(card);
            cards[i] = card;
        }
        return cards;
    }

    /**
     * Remove one or more cards from the hand.
     *
     * @param cards Card or Cards to remove
     */
    public void discard(Card... cards) throws Exception {
        for (Card c : cards) {
            if (!this.cards.remove(c)) {
                throw new Exception("Logic Error. Tried to discard a non existing card.");
            }
        }
    }

    /**
     * Initialize and generate the required hand information to manage the
     * HandRanker. Init rankDict; init and fill ocurDict; init and calculate
     * weight; init and calculate suitId
     */
    public void generateRankerInformation() {
        this.rankDict = new HashMap();
        this.ocurDict = new HashMap();
        this.weight = 1;
        this.suitId = 1;

        for (Card card : this.cards) {
            // generate the map with elements <key = Rank, value = Rank occurrences in this Hand>
            if (!this.ocurDict.containsKey(card.getRank())) {
                this.ocurDict.put(card.getRank(), 1);
            } else {
                this.ocurDict.replace(card.getRank(), (int) this.ocurDict.get(card.getRank()) + 1);
            }

            // compute the product of the weight prime numbers of the cards
            this.weight *= card.getRankWeight();

            // compute the product of the suit ids of the cards
            this.suitId *= card.getSuitId();
        }
    }

    /**
     * Put an element <HandRank, Rank> to the rankDict. This method is necessary
     * to fill the rankDict according to the HandRank.
     *
     * @param handRank HandRank that is the key in the map
     * @param rank Rank that is the value in the map
     */
    public void putIntoRankDict(HandRank handRank, Rank rank) {
        this.rankDict.put(handRank, rank);
    }

    /**
     * Compare this hand with other in order to know if this hand wins. Returns
     * true if this hand wins the other.
     *
     * @param other Hand to compare with this
     * @return boolean
     */
    public boolean wins(Hand other) {
        return this.compareTo(other) > 0;
    }

    /**
     * Compare this hand with other in order to know if we have a tie case.
     * Returns true if this hand and the other are the same value.
     *
     * @param other Hand to compare with this
     * @return boolean
     */
    public boolean ties(Hand other) {
        return this.compareTo(other) == 0;
    }

    /**
     * Compare this hand with other in order to know if this hand loses. Returns
     * true if the other hand wins this.
     *
     * @param other Hand to compare with this
     * @return boolean
     */
    public boolean loses(Hand other) {
        return this.compareTo(other) < 0;
    }

    /**
     * General method to get a set of keys that have the same value in a map.
     *
     * @param <T> Object key
     * @param <E> Object value
     * @param map Map to get the keys from
     * @param value Object that is the value of the keys we want to get
     *
     * @return List of keys
     */
    public static <T, E> List<T> getKeysByValue(Map<T, E> map, E value) {
        List<T> keys = new ArrayList<>();
        for (Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                keys.add(entry.getKey());
            }
        }
        return keys;
    }

    @Override
    public int compareTo(Hand other) {
        HandRank mHandRank = getHandRank(this);
        HandRank oHandRank = getHandRank(other);

        if (mHandRank.wins(oHandRank)) {
            return 1;
        }
        if (mHandRank.loses(oHandRank)) {
            return -1;
        } else {
            // Tie case: both handrank are the same
            switch (mHandRank) {
                case HIGH_CARD:
                    return this.caseHighCard(this.cards, other.cards);

                case ONE_PAIR:
                    return this.caseOnePair(other);

                case TWO_PAIR:
                    return this.caseTwoPair(other);

                case THREE_OF_A_KIND:
                    return this.caseThreeOfAKind(other);

                case STRAIGHT:
                    return this.caseStraight(other);

                case FLUSH:
                    return this.caseHighCard(this.cards, other.cards);

                case FULL_HOUSE:
                    return this.caseFullHouse(other);

                case FOUR_OF_A_KIND:
                    return this.caseFourOfAKind(other);

                case STRAIGHT_FLUSH:
                    return this.caseStraightFlush(other);

                default: // This case is never reached
                    return -1;
            }
        }
    }

    @Override
    public String toString() {
        return this.cards.toString();
    }

    /**
     * Compare my highest card with the highest card of the other. If they are
     * equal, compare the next highest cards. If all are equal, return tie.
     *
     * @param mine List of the cards I want to compare
     * @param other List of the cards of the opponent
     *
     * @return int (1 if my card is higher, -1 if the other card is higher, 0 if
     * tie)
     */
    private int caseHighCard(List<Card> mine, List<Card> other) {
        Card mCard, oCard;
        do {
            mCard = this.getHighCard(mine);
            oCard = this.getHighCard(other);

        } while (mCard != null && mCard.compareTo(oCard) == 0);
        if (mCard == null) {
            return 0;
        }
        return mCard.compareTo(oCard);
    }

    /**
     * Compare the pairs. If they are equal compair the remaining cards.
     *
     * @param other Hand of the opponent
     *
     * @return int
     */
    private int caseOnePair(Hand other) {
        Rank mRank = (Rank) this.rankDict.get(HandRank.ONE_PAIR);
        Rank oRank = (Rank) other.rankDict.get(HandRank.ONE_PAIR);

        int comp = mRank.compareTo(oRank);
        if (comp != 0) {
            return comp;
        }

        List mRemainingCards = this.removeByRank(this.cards, mRank);
        List oRemainingCards = this.removeByRank(other.cards, oRank);
        return this.caseHighCard(mRemainingCards, oRemainingCards);
    }

    /**
     * Compare the pairs. If they are equal compair the remaining cards.
     *
     * @param other Hand of the opponent
     *
     * @return int
     */
    private int caseTwoPair(Hand other) {
        // Compare first the highest pair of both hands
        Rank mHighRank = (Rank) this.rankDict.get(HandRank.ONE_PAIR);
        Rank oHighRank = (Rank) other.rankDict.get(HandRank.ONE_PAIR);

        int compHigh = mHighRank.compareTo(oHighRank);
        if (compHigh != 0) {
            return compHigh;
        }

        // Compare the lowest pair of both hands
        Rank mLowRank = (Rank) this.rankDict.get(HandRank.TWO_PAIR);
        Rank oLowRank = (Rank) other.rankDict.get(HandRank.TWO_PAIR);

        int compLow = mLowRank.compareTo(oLowRank);
        if (compLow != 0) {
            return compLow;
        }

        // Compair the remaining cards
        Card mRemainingCard = this.removeByRank(this.removeByRank(this.cards, mHighRank), mLowRank).get(0);
        Card oRemainingCard = this.removeByRank(this.removeByRank(other.cards, oHighRank), oLowRank).get(0);
        return mRemainingCard.compareTo(oRemainingCard);
    }

    /**
     * Compare the trios. If they are equal compair the remaining cards.
     *
     * @param other Hand of the opponent
     *
     * @return int
     */
    private int caseThreeOfAKind(Hand other) {
        Rank mRank = (Rank) this.rankDict.get(HandRank.THREE_OF_A_KIND);
        Rank oRank = (Rank) other.rankDict.get(HandRank.THREE_OF_A_KIND);

        int comp = mRank.compareTo(oRank);
        if (comp != 0) {
            return comp;
        }

        List mRemainingCards = this.removeByRank(this.cards, mRank);
        List oRemainingCards = this.removeByRank(other.cards, oRank);
        return this.caseHighCard(mRemainingCards, oRemainingCards);
    }

    /**
     * Compare the highest card.
     *
     * @param other Hand of the opponent
     *
     * @return int
     */
    private int caseStraight(Hand other) {
        return ((Rank) this.rankDict.get(HandRank.HIGH_CARD)).compareTo(
                ((Rank) other.rankDict.get(HandRank.HIGH_CARD)));
    }

    /**
     * Compare the trios. If they are equals compare the pairs.
     *
     * @param other Hand of the opponent
     *
     * @return int
     */
    private int caseFullHouse(Hand other) {
        int compTrios = ((Rank) this.rankDict.get(HandRank.THREE_OF_A_KIND)).compareTo(
                ((Rank) other.rankDict.get(HandRank.THREE_OF_A_KIND)));
        int compPairs = ((Rank) this.rankDict.get(HandRank.ONE_PAIR)).compareTo(
                ((Rank) other.rankDict.get(HandRank.ONE_PAIR)));

        if (compTrios != 0) {
            return compTrios;
        }
        return compPairs;
    }

    /**
     * Compare the quatrains.
     *
     * @param other
     * @return
     */
    private int caseFourOfAKind(Hand other) {
        return ((Rank) this.rankDict.get(HandRank.FOUR_OF_A_KIND)).compareTo(
                ((Rank) other.rankDict.get(HandRank.FOUR_OF_A_KIND)));
    }

    /**
     * Compare the highest card.
     *
     * @param other Hand of the opponent
     *
     * @return int
     */
    private int caseStraightFlush(Hand other) {
        return ((Rank) this.rankDict.get(HandRank.HIGH_CARD)).compareTo(
                ((Rank) other.rankDict.get(HandRank.HIGH_CARD)));
    }

    /**
     * Get the highest card of a list of cards and remove it from the list.
     *
     * @param cards List of cards
     *
     * @return Card
     */
    private Card getHighCard(List<Card> cards) {
        if (cards.isEmpty()) {
            return null;
        }
        Collections.sort(cards);
        return cards.remove(cards.size() - 1);
    }

    /**
     * Remove all the cards of a list that have a specific rank. Returns the
     * same list without the cards with this rank.
     *
     * @param cards List of cards to remove the specific rank
     * @param rank Rank to remove from the list
     *
     * @return List of Cards
     */
    private List<Card> removeByRank(List<Card> cards, Rank rank) {
        List aux = new ArrayList(cards);
        for (Card card : cards) {
            if (card.getRank().equals(rank)) {
                aux.remove(card);
            }
        }
        return aux;
    }

    public void dumpArray(Card[] cards) {
        for(int i = 0; i < 5; i++)
            cards[i] = this.cards.get(i);
    }

}
