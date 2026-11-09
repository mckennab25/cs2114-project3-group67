package krustykrab;

import java.util.List;

// -------------------------------------------------------------------------
/**
 * Read-only interface exposing game state to observers and views, gets a
 * read-only snapshot of the game's current status
 * 
 * @author mckennabryan
 * @version Nov 9, 2026
 */
public interface GameState
{
    // ----------------------------------------------------------
    /**
     * Checks the current tick count of an active shift
     * 
     * @return the number of ticks elapsed since started
     */
    int getTick();


    // ----------------------------------------------------------
    /**
     * Gets remaining ticks before the current game shift ends
     * 
     * @return number of ticks remaining
     */
    int getTicksLeft();


    // ----------------------------------------------------------
    /**
     * Gets the players cumulative score
     * 
     * @return current score from completed orders and incorrect step penalties
     */
    int getScore();


    // ----------------------------------------------------------
    /**
     * A snapshot of all the customers waiting in line. Returned as a List
     * instead of LinkedQueue so the view doesn't able to alter the game state
     * 
     * @return an unmodifiable list of customers with FIFO
     */
    List<Customer> getLine();


    // ----------------------------------------------------------
    /**
     * Boolean of whether Plankton is attacking the Krusty Krab
     * 
     * @return true if raid is active and needs defense, false if otherwise
     */
    boolean isRaidActive();


    // ----------------------------------------------------------
    /**
     * Number of ticks remaining to defend against the raid
     * 
     * @return number of ticks left in defense before the formula is stolen or 0
     *             if no raid is active
     */
    int getRaidTicksLeft();
}
