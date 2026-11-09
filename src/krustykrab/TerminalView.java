package krustykrab;

import java.io.PrintStream;
import java.util.EnumSet;
import java.util.List;

// -------------------------------------------------------------------------
/**
 * Prints the Krusty Krab game to a terminal. The view sees the game only
 * through {@link GameState}, learns of changes through
 * {@link GameObserver#update}, and never changes the game.
 *
 * @author Asher
 */
public class TerminalView implements GameObserver
{
    private static final EnumSet<GameEvent> END_EVENTS = EnumSet.of(
        GameEvent.WON,
        GameEvent.LOST_FORMULA,
        GameEvent.LOST_WALKOUTS,
        GameEvent.QUIT);

    private final PrintStream out;


    // ----------------------------------------------------------
    /**
     * Creates a view that prints to the given stream.
     *
     * @param out
     *            where all output is printed
     * @throws IllegalArgumentException
     *             if out is null
     */
    public TerminalView(PrintStream out)
    {
        if (out == null)
        {
            throw new IllegalArgumentException("Output stream cannot be null");
        }
        this.out = out;
    }


    // ----------------------------------------------------------
    /**
     * Prints a one-line message for the event, followed by the end screen if
     * the event ended the game.
     *
     * @param state
     *            the current game state
     * @param event
     *            what just happened
     */
    @Override
    public void update(GameState state, GameEvent event)
    {
        out.println(message(event));
        if (END_EVENTS.contains(event))
        {
            showEndScreen(state);
        }
    }


    // ----------------------------------------------------------
    /**
     * Prints the numbered menu: one choice per ingredient, then Defend and
     * Quit.
     */
    public void showMenu()
    {
        Ingredient[] ingredients = Ingredient.values();
        out.println("Menu:");
        for (int i = 0; i < ingredients.length; i++)
        {
            out.println("  " + (i + 1) + ". " + label(ingredients[i]));
        }
        out.println("  7. Defend the formula");
        out.println("  8. Quit (or q)");
    }


    // ----------------------------------------------------------
    /**
     * Prints a rejection message for bad input.
     *
     * @param message
     *            what was wrong with the input
     */
    public void showError(String message)
    {
        out.println("Error: " + message);
    }


    // ----------------------------------------------------------
    /**
     * Prints the status screen: time, score, any active raid, and the line
     * with the front customer's order in detail.
     *
     * @param state
     *            the game state to display
     */
    public void showStats(GameState state)
    {
        out.println("---- Tick " + state.getTick() + " | "
            + state.getTicksLeft() + " left | Score " + state.getScore()
            + " ----");
        if (state.isRaidActive())
        {
            out.println("!!! PLANKTON RAID !!! Ticks left to defend: "
                + state.getRaidTicksLeft());
        }

        List<Customer> line = state.getLine();
        if (line.isEmpty())
        {
            out.println("Line: (empty)");
            return;
        }

        Customer front = line.get(0);
        List<Ingredient> steps = front.getSteps();
        out.println("Serving customer " + front.getId() + " (patience "
            + front.getPatience() + "): next is " + label(steps.get(0))
            + " (steps left: " + steps.size() + ")");
        for (int i = 1; i < line.size(); i++)
        {
            out.println("Waiting: customer " + line.get(i).getId()
                + " (patience " + line.get(i).getPatience() + ")");
        }
    }


    // --- Private helpers ---

    private void showEndScreen(GameState state)
    {
        out.println("==== SHIFT OVER ====");
        out.println("Final score: " + state.getScore());
        out.println("Ticks survived: " + state.getTick());
    }


    private static String label(Ingredient ingredient)
    {
        return ingredient.name().replace('_', ' ').toLowerCase();
    }


    private static String message(GameEvent event)
    {
        switch (event)
        {
            case CORRECT_STEP:
                return "Correct ingredient.";
            case ORDER_SERVED:
                return "Order served!";
            case WRONG_INGREDIENT:
                return "Wrong ingredient! -2 points.";
            case NO_CUSTOMER:
                return "Nobody is in line.";
            case FORMULA_REPELLED:
                return "You drove Plankton off!";
            case NO_THREAT:
                return "There is no raid to defend against.";
            case CUSTOMER_ARRIVED:
                return "A customer joined the line.";
            case CUSTOMER_WALKED_OUT:
                return "A customer ran out of patience and left!";
            case RAID_STARTED:
                return "Plankton is raiding! Choose Defend now!";
            case WON:
                return "You survived the shift!";
            case LOST_FORMULA:
                return "Plankton stole the secret formula!";
            case LOST_WALKOUTS:
                return "Too many customers walked out!";
            case QUIT:
            default:
                return "You quit the shift.";
        }
    }
}
