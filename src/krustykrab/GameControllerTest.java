package krustykrab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Scanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// -------------------------------------------------------------------------
/**
 * Tests {@link GameController}: input parsing on its own, and whole games
 * played from scripted input with a Plankton chance of 0.
 *
 * @author Mckenna Bryan
 * @author Connor
 * @author Asher
 */
public class GameControllerTest
{
    private KrustyKrabGame game;
    private ByteArrayOutputStream bytes;


    // ----------------------------------------------------------
    /** Creates a fresh game whose view prints to a captured buffer. */
    @BeforeEach
    public void setUp()
    {
        game = new KrustyKrabGame(1, 0);
        bytes = new ByteArrayOutputStream();
    }


    private void play(String input)
    {
        TerminalView view = new TerminalView(new PrintStream(bytes));
        game.addObserver(view);
        new GameController(game, view, new Scanner(input)).run();
    }


    // ----------------------------------------------------------
    /** Digits 1-8 are choices, and spaces and letter case do not matter. */
    @Test
    public void parseChoiceAcceptsValidInput()
    {
        assertEquals(3, GameController.parseChoice(" 3 "));
        assertEquals(1, GameController.parseChoice("1"));
        assertEquals(7, GameController.parseChoice("7"));
        assertEquals(8, GameController.parseChoice("8"));
        assertEquals(8, GameController.parseChoice("q"));
        assertEquals(8, GameController.parseChoice("QUIT"));
        assertEquals(8, GameController.parseChoice(" Quit "));
    }


    // ----------------------------------------------------------
    /** Anything that is not a menu choice is rejected. */
    @Test
    public void parseChoiceRejectsBadInput()
    {
        String[] bad = {"A", "", "   ", "0", "9", "-1", "29", "3.5", "quitt",
            "12", null};
        for (String input : bad)
        {
            assertThrows(IllegalArgumentException.class,
                () -> GameController.parseChoice(input), "" + input);
        }
    }


    // ----------------------------------------------------------
    /** A string over 20 characters is rejected as too long. */
    @Test
    public void parseChoiceRejectsLongInput()
    {
        IllegalArgumentException e = assertThrows(
            IllegalArgumentException.class,
            () -> GameController.parseChoice("123456789012345678901"));
        assertTrue(e.getMessage().contains("too long"));
    }


    // ----------------------------------------------------------
    /** Six correct ingredients serve the order, then the player quits. */
    @Test
    public void runServesAnOrderThenQuits()
    {
        play("1\n2\n3\n4\n5\n6\nq\n");
        assertEquals(25, game.getScore());
        assertEquals(6, game.getTick());
        assertEquals(GameEvent.QUIT, game.getOutcome());
        assertTrue(bytes.toString().contains("Order served!"));
        assertTrue(bytes.toString().contains("SHIFT OVER"));
    }


    // ----------------------------------------------------------
    /** Bad input shows an error, costs no tick, and play continues. */
    @Test
    public void badInputCostsNothing()
    {
        play("x\n\n99\n123456789012345678901\nq\n");
        assertEquals(0, game.getTick());
        assertEquals(GameEvent.QUIT, game.getOutcome());
        assertTrue(bytes.toString().contains("Choose 1-8"));
        assertTrue(bytes.toString().contains("Input too long"));
    }


    // ----------------------------------------------------------
    /** Running out of input quits cleanly instead of crashing. */
    @Test
    public void endOfInputQuits()
    {
        play("1\n");
        assertEquals(1, game.getTick());
        assertEquals(GameEvent.QUIT, game.getOutcome());
        assertTrue(bytes.toString().contains("SHIFT OVER"));
    }


    // ----------------------------------------------------------
    /** Defending with no raid uses a tick, and idling loses to walkouts. */
    @Test
    public void idlePlayerLosesAndLeftoverInputIsIgnored()
    {
        play(String.join("", Collections.nCopies(60, "7\n")));
        assertEquals(GameEvent.LOST_WALKOUTS, game.getOutcome());
        assertTrue(game.getTick() < 60);
    }


    // ----------------------------------------------------------
    /** A wrong ingredient is a legal move: it costs 2 points and a tick. */
    @Test
    public void wrongIngredientCostsPoints()
    {
        play("6\nq\n");
        assertEquals(-2, game.getScore());
        assertTrue(bytes.toString().contains("Wrong ingredient"));
    }
}
