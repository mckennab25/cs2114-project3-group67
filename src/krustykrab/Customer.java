package krustykrab;

import java.util.List;

public class Customer
{
    private final int id;
    private int patience;
    private final LinkedQueue<Ingredient> steps;
    private final boolean patrick;
    private final int rewardPoints;
    
    public Customer(int id, List<Ingredient> recipe, int patience)
    {
        this(id, recipe, patience, false);
    }

    public Customer(
        int id,
        List<Ingredient> recipe,
        int patience,
        boolean patrick)
    {
        if (recipe == null || recipe.isEmpty())
        {
            throw new IllegalArgumentException(
                "Recipe must have ingredients");
        }

        if (patience <= 0)
        {
            throw new IllegalArgumentException(
                "Patience must be positive");
        }

        this.id = id;
        this.patrick = patrick;
        this.patience = patrick ? 1 : patience;

        // Example scoring: regular customers = 10, Patrick = 30.
        this.rewardPoints = patrick ? 30 : 10;

        steps = new LinkedQueue<>();

        for (Ingredient ingredient : recipe)
        {
            steps.enqueue(ingredient);
        }
    }
    
    public boolean apply(Ingredient ing)
    {
        if (ing == null)
        {
            throw new IllegalArgumentException(
                "Ingredient cannot be null");
        }

        if (hasWalkedOut() || isServed() || steps.peek() != ing)
        {
            return false;
        }

        steps.dequeue();
        return true;
    }
    
    public boolean isServed()
    {
        return steps.isEmpty();
    }

    public boolean hasWalkedOut()
    {
        return patience == 0;
    }

    void losePatience()
    {
        if (patience > 0)
        {
            patience--;
        }
    }
    
    public int getId()
    {
        return id;
    }

    public int getPatience()
    {
        return patience;
    }
    
    public List<Ingredient> getSteps()
    {
        return steps.snapshot();
    }
    
    public boolean isPatrick()
    {
        return patrick;
    }

    public String getName()
    {
        return patrick ? "Patrick" : "Customer " + id;
    }

    public int getRewardPoints()
    {
        return rewardPoints;
    }
    
    
    
    
    
    //~ Fields ................................................................

    //~ Constructors ..........................................................

    //~Public  Methods ........................................................

}
