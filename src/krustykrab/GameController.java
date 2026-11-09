package krustykrab;

import java.util.Scanner;

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
        view.showStats(game);
        while (!game.isOver())
        {
            view.showMenu();
            if (!in.hasNextLine())
            {
                game.quit();
                break;
            }

            int choice;
            try
            {
                choice = parseChoice(in.nextLine());
            }
            catch (IllegalArgumentException e)
            {
                view.showError(e.getMessage());
                continue;
            }

            act(choice);
            if (!game.isOver())
            {
                view.showStats(game);
            }
        }
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
}
