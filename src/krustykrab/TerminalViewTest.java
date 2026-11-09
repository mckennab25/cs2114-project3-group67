package krustykrab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// -------------------------------------------------------------------------
/**
 * Tests {@link TerminalView} against a hand-built {@link GameState}, with no
 * game, controller, or real terminal involved.
 *
 * @author Asher
 */
public class TerminalViewTest
{
    private ByteArrayOutputStream bytes;
    private TerminalView view;
    private FakeState state;


    /** Fake game state the tests can set directly. */
    private static class FakeState implements GameState
    {
        int tick = 0;
        int score = 0;
        int raidTicksLeft = 0;
        List<Customer> line = new ArrayList<>();


        public int getTick()
        {
            return tick;
        }


        public int getTicksLeft()
        {
            return 60 - tick;
        }


        public int getScore()
        {
            return score;
        }


        public List<Customer> getLine()
        {
            return line;
        }


        public boolean isRaidActive()
        {
            return raidTicksLeft > 0;
        }


        public int getRaidTicksLeft()
        {
            return raidTicksLeft;
        }
    }


    // ----------------------------------------------------------
    /** Creates a fresh view writing to a captured buffer. */
    @BeforeEach
    public void setUp()
    {
        bytes = new ByteArrayOutputStream();
        view = new TerminalView(new PrintStream(bytes));
        state = new FakeState();
    }


    private String output()
    {
        return bytes.toString();
    }


    private Customer customer(int id, int patience)
    {
        return new Customer(id, Arrays.asList(Ingredient.values()), patience);
    }


    // ----------------------------------------------------------
    /** A null stream is rejected. */
    @Test
    public void constructorRejectsNull()
    {
        assertThrows(IllegalArgumentException.class,
            () -> new TerminalView(null));
    }


    // ----------------------------------------------------------
    /** Every non-ending event prints exactly one line and no status screen. */
    @Test
    public void updatePrintsOneLineForActionAndStateEvents()
    {
        List<GameEvent> events = Arrays.asList(GameEvent.CORRECT_STEP,
            GameEvent.ORDER_SERVED, GameEvent.WRONG_INGREDIENT,
            GameEvent.NO_CUSTOMER, GameEvent.FORMULA_REPELLED,
            GameEvent.NO_THREAT, GameEvent.CUSTOMER_ARRIVED,
            GameEvent.CUSTOMER_WALKED_OUT, GameEvent.RAID_STARTED);
        for (GameEvent event : events)
        {
            bytes.reset();
            view.update(state, event);
            assertEquals(1, output().trim().split("\\R").length, "" + event);
        }
    }


    // ----------------------------------------------------------
    /** Each ending event prints the end screen with score and ticks. */
    @Test
    public void updateShowsEndScreenForEveryOutcome()
    {
        state.score = 42;
        state.tick = 17;
        List<GameEvent> ends = Arrays.asList(GameEvent.WON,
            GameEvent.LOST_FORMULA, GameEvent.LOST_WALKOUTS, GameEvent.QUIT);
        for (GameEvent event : ends)
        {
            bytes.reset();
            view.update(state, event);
            assertTrue(output().contains("SHIFT OVER"), "" + event);
            assertTrue(output().contains("Final score: 42"), "" + event);
            assertTrue(output().contains("Ticks survived: 17"), "" + event);
        }
    }


    // ----------------------------------------------------------
    /** The messages tell the player what happened. */
    @Test
    public void updateMessagesDescribeTheEvent()
    {
        view.update(state, GameEvent.WRONG_INGREDIENT);
        view.update(state, GameEvent.RAID_STARTED);
        assertTrue(output().contains("Wrong ingredient"));
        assertTrue(output().contains("Defend"));
    }


    // ----------------------------------------------------------
    /** The menu numbers all six ingredients, then Defend and Quit. */
    @Test
    public void showMenuListsEveryChoice()
    {
        view.showMenu();
        assertTrue(output().contains("1. bottom bun"));
        assertTrue(output().contains("2. patty"));
        assertTrue(output().contains("3. cheese"));
        assertTrue(output().contains("4. lettuce"));
        assertTrue(output().contains("5. tomato"));
        assertTrue(output().contains("6. top bun"));
        assertTrue(output().contains("7. Defend"));
        assertTrue(output().contains("8. Quit"));
    }


    // ----------------------------------------------------------
    /** An error message is printed as given. */
    @Test
    public void showErrorPrintsMessage()
    {
        view.showError("Choose 1-8");
        assertTrue(output().contains("Choose 1-8"));
    }


    // ----------------------------------------------------------
    /** An empty line prints the header and an empty-line note. */
    @Test
    public void showStatsWithEmptyLine()
    {
        state.tick = 5;
        state.score = 12;
        view.showStats(state);
        assertTrue(output().contains("Tick 5"));
        assertTrue(output().contains("55 left"));
        assertTrue(output().contains("Score 12"));
        assertTrue(output().contains("(empty)"));
        assertFalse(output().contains("RAID"));
    }


    // ----------------------------------------------------------
    /** The front customer is shown in detail and the rest briefly. */
    @Test
    public void showStatsWithCustomers()
    {
        state.line.add(customer(1, 18));
        state.line.add(customer(2, 20));
        state.line.get(0).apply(Ingredient.BOTTOM_BUN);
        view.showStats(state);
        assertTrue(output().contains(
            "Serving customer 1 (patience 18): next is patty (steps left: 5)"));
        assertTrue(output().contains("Waiting: customer 2 (patience 20)"));
        assertFalse(output().contains("(empty)"));
    }


    // ----------------------------------------------------------
    /** An active raid shows its countdown. */
    @Test
    public void showStatsWithActiveRaid()
    {
        state.raidTicksLeft = 2;
        view.showStats(state);
        assertTrue(output().contains("PLANKTON RAID"));
        assertTrue(output().contains("Ticks left to defend: 2"));
    }
}
