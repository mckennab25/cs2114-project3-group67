package krustykrab;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class KrustyKrabGame
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
     * @param seed the odds of plankton attack
     * @param planktonChance change of Plankton raid
     */
    public KrustyKrabGame(long seed, int planktonChance) {
        if (planktonChance < 0 || planktonChance > 100) {
            throw new IllegalArgumentException("Plankton chance must be between 0 and 100");
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
     * @param o the observer to register
     * @throw IllegalArguementException if param is null
     */
    public void addObserver(GameObserver o) {
        if (o == null) {
            throw new IllegalArgumentException("Observer cannot be null");
        }
        observers.add(o);
    }
}
