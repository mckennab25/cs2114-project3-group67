package krustykrab;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class KrustyKrabGame implements GameState
{
    // ~ Fields ................................................................
    // default fields to establish the game (ticks per customer, losing
    // condition, total shift duration)

    private static final int SHIFT_DURATION = 60;
    private static final int ARRIVAL_INTERVAL = 4;
    public static final int MAX_LINE_SIZE = 6;
    public static final int DEFAULT_PATIENCE = 20;
    public static final int MAX_WALKOUTS = 3;
    public static final int RAID_WINDOW = 3;

    private final LinkedQueue<Customer> line;
    private final List<GameObserver> observers;
    private final Random random;
    private final int planktonChance;

    private int tick;
    private int score;
    private int served;
    private int walkouts;
    private int raidTicksLeft;
    private int nextCustomerId;
    private GameEvent outcome;

    // ~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Constructor for the game
     * 
     * @param seed
     *            the odds of plankton attack
     * @param planktonChance
     *            change of Plankton raid
     */
    public KrustyKrabGame(long seed, int planktonChance)
    {
        if (planktonChance < 0 || planktonChance > 100)
        {
            throw new IllegalArgumentException(
                "Plankton chance must be between 0 and 100");
        }

        this.random = new Random(seed);
        this.planktonChance = planktonChance;
        this.line = new LinkedQueue<>();
        this.observers = new ArrayList<>();

        this.tick = 0;
        this.score = 0;
        this.served = 0;
        this.walkouts = 0;
        this.raidTicksLeft = 0;
        this.nextCustomerId = 1;
        this.outcome = null;

        // Start game with one customer in line
        addCustomerToLine();
    }
    // ~Public Methods ........................................................


    // ----------------------------------------------------------
    /**
     * Registers a new observer whenever a game event occurs
     * 
     * @param o
     *            the observer to register
     * @throw IllegalArguementException if param is null
     */
    public void addObserver(GameObserver o)
    {
        if (o == null)
        {
            throw new IllegalArgumentException("Observer cannot be null");
        }
        observers.add(o);
    }


    // ----------------------------------------------------------
    /**
     * Applies an ingredient to the customer currently at the front of the line
     * and advances the turn by one tick.
     * <p>
     * Scoring: A correct step that finishes the order awards 10 points plus
     * remaining customer patience. An incorrect step deducts 2 points.
     * </p>
     *
     * @param ing
     *            the ingredient selected by the player
     * @return GameEvent saying what happened when ing is added (CORRECT_STEP,
     *             ORDER_SERVED, WRONG_INGREDIENT, NO_CUSTOMER)
     */
    public GameEvent addIngredient(Ingredient ing)
    {
        if (ing == null)
        {
            throw new IllegalArgumentException("Ingredient cannot be null");
        }
        if (isOver())
        {
            throw new IllegalStateException(
                "Cannot perform actions after game over");
        }

        GameEvent actionResult;

        if (line.isEmpty())
        {
            actionResult = GameEvent.NO_CUSTOMER;
        }
        else
        {
            Customer current = line.peek();
            boolean correct = current.apply(ing);

            if (correct)
            {
                if (current.isServed())
                {
                    score += 10 + current.getPatience();
                    line.dequeue();
                    served++;
                    actionResult = GameEvent.ORDER_SERVED;
                }
                else
                {
                    actionResult = GameEvent.CORRECT_STEP;
                }
            }
            else
            {
                score -= 2;
                actionResult = GameEvent.WRONG_INGREDIENT;
            }
        }

        runTick(actionResult);
        return actionResult;
    }


    /**
     * Attempts to defend the Krabby Patty secret formula from an active
     * Plankton raid and advances the turn by one tick.
     *
     * @return {@link GameEvent#FORMULA_REPELLED} if an active raid was
     *             successfully defended; {@link GameEvent#NO_THREAT} if no raid
     *             was currently active
     * @throws IllegalStateException
     *             if the game is already over
     */
    public GameEvent defendFormula()
    {
        if (isOver())
        {
            throw new IllegalStateException(
                "Cannot perform actions after game over");
        }

        GameEvent actionResult;
        if (isRaidActive())
        {
            raidTicksLeft = 0;
            actionResult = GameEvent.FORMULA_REPELLED;
        }
        else
        {
            actionResult = GameEvent.NO_THREAT;
        }

        runTick(actionResult);
        return actionResult;
    }


    /**
     * Ends the game immediately with a quit state.
     * <p>
     * If the game is already over, calling this method has no effect.
     * </p>
     */
    public void quit()
    {
        if (!isOver())
        {
            outcome = GameEvent.QUIT;
            notifyObservers(GameEvent.QUIT);
        }
    }


    /**
     * Checks whether the game has concluded due to victory, loss, or user quit.
     *
     * @return {@code true} if an end game condition has been reached;
     *             {@code false} otherwise
     */
    public boolean isOver()
    {
        return outcome != null;
    }


    /**
     * Retrieves the terminal outcome of the game.
     *
     * @return the {@link GameEvent} outcome (e.g., WON, LOST_FORMULA,
     *             LOST_WALKOUTS, QUIT), or {@code null} if the game is still in
     *             progress
     */
    public GameEvent getOutcome()
    {
        return outcome;
    }
    
 // --- Private Helper Mechanics ---

    /**
     * Executes the internal update cycle for a single tick action.
     * 
     * <p>Processes observers, ticks down active raids, rolls for new raids, ages customer patience,
     * checks for walkouts, handles new customer arrivals, and checks shift completion.</p>
     *
     * @param actionResult the immediate outcome event resulting from the player's action
     */
    private void runTick(GameEvent actionResult) {
        // Notify result of the immediate action taken
        notifyObservers(actionResult);

        tick++;

        // 1. Process active raid window
        if (isRaidActive()) {
            raidTicksLeft--;
            if (raidTicksLeft <= 0) {
                outcome = GameEvent.LOST_FORMULA;
                notifyObservers(GameEvent.LOST_FORMULA);
                return;
            }
        } else {
            // Roll for new raid
            if (random.nextInt(100) < planktonChance) {
                raidTicksLeft = RAID_WINDOW;
                notifyObservers(GameEvent.RAID_STARTED);
            }
        }

        // 2. Age customers in line (rotate queue)
        int currentSize = line.getSize();
        for (int i = 0; i < currentSize; i++) {
            Customer c = line.dequeue();
            c.losePatience(); // Package-private access inside model package

            if (c.hasWalkedOut()) {
                walkouts++;
                notifyObservers(GameEvent.CUSTOMER_WALKED_OUT);
                if (walkouts >= MAX_WALKOUTS && outcome == null) {
                    outcome = GameEvent.LOST_WALKOUTS;
                    notifyObservers(GameEvent.LOST_WALKOUTS);
                    return;
                }
            } else {
                line.enqueue(c);
            }
        }

        // 3. Arrivals every ARRIVAL_INTERVAL ticks
        if (tick % ARRIVAL_INTERVAL == 0) {
            if (line.getSize() < MAX_LINE_SIZE) {
                addCustomerToLine();
                notifyObservers(GameEvent.CUSTOMER_ARRIVED);
            }
        }

        // 4. Shift check
        if (tick >= SHIFT_DURATION && outcome == null) {
            outcome = GameEvent.WON;
            notifyObservers(GameEvent.WON);
        }
    }
    
    /**
     * Emits a game event update to all currently registered observers.
     *
     * @param event the event to broadcast
     */
    private void notifyObservers(GameEvent event) {
        for (GameObserver o : observers) {
            o.update(this, event);
        }
    }
    
    /** NEED TO CHECK IF THIS IS THE ONLY RECIPE
     * Creates a new customer with the standard 6-step recipe and adds them to the end of the customer queue.
     */
    private void addCustomerToLine() {
        List<Ingredient> standardRecipe = Arrays.asList(
            Ingredient.BOTTOM_BUN,
            Ingredient.PATTY,
            Ingredient.CHEESE,
            Ingredient.LETTUCE,
            Ingredient.TOMATO,
            Ingredient.TOP_BUN
        );
        line.enqueue(new Customer(nextCustomerId++, standardRecipe, DEFAULT_PATIENCE));
    }
    
 // --- GameState Interface Getters ---

    /** {@inheritDoc} */
    @Override
    public int getTick() { 
        return tick; 
    }

    /** {@inheritDoc} */
    @Override
    public int getTicksLeft() { 
        return SHIFT_DURATION - tick; 
    }

    /** {@inheritDoc} */
    @Override
    public int getScore() { 
        return score; 
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Returns a defensive copy of the customer line so external views cannot modify internal state.</p>
     */
    @Override
    public List<Customer> getLine() {
        List<Customer> copy = new ArrayList<>();
        int size = line.getSize();
        for (int i = 0; i < size; i++) {
            Customer c = line.dequeue();
            copy.add(c);
            line.enqueue(c);
        }
        return copy;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isRaidActive() { 
        return raidTicksLeft > 0; 
    }

    /** {@inheritDoc} */
    @Override
    public int getRaidTicksLeft() { 
        return raidTicksLeft; 
    }
}
}
