
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import poker5cardgame.game.Card;
import poker5cardgame.game.Hand;
import poker5cardgame.game.HandRanker;
import poker5cardgame.game.HandRanker.HandRank;

public class RankerUnitTest {

    Hand handA;
    Hand handB;

    public RankerUnitTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        handA = new Hand();
        handA.putCards(new Card(Card.Suit.DIAMONDS, Card.Rank.TWO));
        handA.putCards(new Card(Card.Suit.DIAMONDS, Card.Rank.THREE));
        handA.putCards(new Card(Card.Suit.DIAMONDS, Card.Rank.FOUR));
        handA.putCards(new Card(Card.Suit.DIAMONDS, Card.Rank.FIVE));
        handA.putCards(new Card(Card.Suit.DIAMONDS, Card.Rank.SIX));
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testRanker() {
        HandRank rank = HandRanker.getHandRank(handA);
        assertEquals(HandRank.STRAIGHT_FLUSH, rank);
    }
}
