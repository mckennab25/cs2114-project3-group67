package krustykrab;

// -------------------------------------------------------------------------
/**
 * A list of all the possible events that can occur during a turn or game state
 * transition. Such as orders being served, customers arriving, etc.
 * 
 * @author mckennabryan
 * @version Nov 9, 2026
 */
public enum GameEvent
{
    // Action results
    CORRECT_STEP,
    ORDER_SERVED,
    WRONG_INGREDIENT,
    NO_CUSTOMER,
    FORMULA_REPELLED,
    NO_THREAT,

    // State events
    CUSTOMER_ARRIVED,
    CUSTOMER_WALKED_OUT,
    RAID_STARTED,

    // End game outcomes
    WON,
    LOST_FORMULA,
    LOST_WALKOUTS,
    QUIT
}
