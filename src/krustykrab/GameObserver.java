package krustykrab;

// -------------------------------------------------------------------------
/**
 * Observer interface for receiving game state change updates.Any class that
 * wants to react when something happens calls this. TerminalView implements
 * GameObserver. Pushes updates whenever an action occurs
 * 
 * @author mckennabryan
 * @version Nov 9, 2026
 */
public interface GameObserver
{
    // ----------------------------------------------------------
    /**
     * Updates the game whenever an action occurs for the terminal
     * 
     * @param state
     *            the current state of the game (the line, ticks, if raid is
     *            active, etc.)
     * @param event
     *            the event that just happened (raid, customer served, walkout,
     *            etc)
     */
    void update(GameState state, GameEvent event);
}
