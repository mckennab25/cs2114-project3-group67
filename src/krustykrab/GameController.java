package krustykrab;

import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

// -------------------------------------------------------------------------
/**
 * Reads the player's choices, validates them, and calls the game. Bad text is
 * rejected here and costs no tick. The end screen is printed by the view when
 * the game announces its outcome.
 *
 * @author Mckenna Bryan
 * @author Connor
 * @author Asher
 */
public class GameController
{
    /** The longest input, after trimming, that is read as a choice. */
    public static final int MAX_INPUT_LENGTH = 20;

    private static final int DEFEND = 7;
    private static final int QUIT = 8;
    private static final int PLANKTON_CHANCE = 5;

    private final KrustyKrabGame game;
    private final TerminalView view;
    private final Scanner in;


    // ----------------------------------------------------------
    /**
     * Creates a controller for one game.
     *
     * @param game
     *            the game to play
     * @param view
     *            the view used for the menu, status screen and errors; it
     *            should already be registered with the game as an observer
     * @param in
     *            where the player's choices are read from
     */
    public GameController(KrustyKrabGame game, TerminalView view, Scanner in)
    {
        this.game = game;
        this.view = view;
        this.in = in;
    }


    // ----------------------------------------------------------
    /**
     * Plays until the game is over: show the menu, read a line, and either
     * re-prompt on bad input or apply the choice. Running out of input quits.
     */
    public void run()
    {
    	long tickNanos = tickDurationNanos();
        final BlockingQueue<String> inputs =
            new LinkedBlockingQueue<>();

        final String endOfInput = new String();

        Thread inputReader = new Thread(() ->
        {
            try
            {
                while (in.hasNextLine())
                {
                    inputs.add(in.nextLine());
                }
            }
            finally
            {
                inputs.add(endOfInput);
            }
        });

        inputReader.setDaemon(true);
        inputReader.start();

        long nextTick = System.nanoTime() + tickNanos;
        Customer previousFront = frontCustomer();

        view.showStats(game);
        view.showMenu();

        try
        {
            while (!game.isOver())
            {
                long wait = Math.max(
                    0L, nextTick - System.nanoTime());

                String raw = inputs.poll(
                    wait, TimeUnit.NANOSECONDS);

                long now = System.nanoTime();
                boolean timeChanged = false;

                while (now >= nextTick && !game.isOver())
                {
                    game.advanceTime();
                    nextTick += tickNanos;
                    timeChanged = true;
                }

                if (game.isOver())
                {
                    break;
                }

                if (raw == endOfInput)
                {
                    game.quit();
                    break;
                }

                if (raw != null)
                {
                    try
                    {
                        act(parseChoice(raw));
                    }
                    catch (IllegalArgumentException e)
                    {
                        view.showError(e.getMessage());
                    }
                }

                if (game.isOver())
                {
                    break;
                }
                long newDuration = tickDurationNanos();

                if (newDuration != tickNanos)
                {
                    tickNanos = newDuration;
                    nextTick = System.nanoTime() + tickNanos;

                    view.showNotice(
                        "SPEED UP! Ticks now last "
                        + TimeUnit.NANOSECONDS.toSeconds(tickNanos)
                        + " seconds.");
                }
                Customer currentFront = frontCustomer();

                if (currentFront != previousFront
                    && currentFront != null
                    && currentFront.isPatrick())
                {
                    nextTick = System.nanoTime() + tickNanos;

                    view.showNotice(
                    	    "*** PATRICK IS AT THE COUNTER! *** "
                    	    + "Finish his order within "
                    	    + TimeUnit.NANOSECONDS.toSeconds(tickNanos)
                    	    + " seconds for 20 BONUS POINTS!");
                }

                previousFront = currentFront;

                if (raw != null)
                {
                    view.showStats(game);
                    view.showMenu();
                }
                else if (timeChanged)
                {
                    System.out.println();
                    view.showStats(game);
                    System.out.print("Choose: ");
                }
            }
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            game.quit();
        }
    }
    
    private Customer frontCustomer()
    {
        java.util.List<Customer> customers = game.getLine();

        return customers.isEmpty() ? null : customers.get(0);
    }


    // ----------------------------------------------------------
    /**
     * Turns raw player input into a menu choice.
     *
     * @param raw
     *            the text the player typed
     * @return 1-6 for an ingredient, 7 for Defend, 8 for Quit ("q" or "quit"
     *             in any case)
     * @throws IllegalArgumentException
     *             if the input is null, blank, too long, or not a choice from
     *             1 to 8
     */
    public static int parseChoice(String raw)
    {
        String text = (raw == null) ? "" : raw.trim();
        if (text.length() > MAX_INPUT_LENGTH)
        {
            throw new IllegalArgumentException("Input too long");
        }
        if (text.equalsIgnoreCase("q") || text.equalsIgnoreCase("quit"))
        {
            return QUIT;
        }
        if (text.matches("[1-8]"))
        {
            return Integer.parseInt(text);
        }
        throw new IllegalArgumentException("Choose 1-8");
    }


    // ----------------------------------------------------------
    /**
     * Builds the game, view and controller, and plays one shift.
     *
     * @param args
     *            unused
     */
    public static void main(String[] args)
    {
        KrustyKrabGame game =
            new KrustyKrabGame(System.currentTimeMillis(), PLANKTON_CHANCE);
        TerminalView view = new TerminalView(System.out);
        game.addObserver(view);
        new GameController(game, view, new Scanner(System.in)).run();
    }


    private void act(int choice)
    {
        if (choice == DEFEND)
        {
            game.defendFormula();
        }
        else if (choice == QUIT)
        {
            game.quit();
        }
        else
        {
            game.addIngredient(Ingredient.values()[choice - 1]);
        }
    }
    
    private long tickDurationNanos()
    {
        int milliseconds = Math.max(
            1000, 5000 - (game.getServed() / 3) * 1000);

        return TimeUnit.MILLISECONDS.toNanos(milliseconds);
    }
}
