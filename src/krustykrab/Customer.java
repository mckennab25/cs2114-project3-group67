package krustykrab;

import java.util.List;

public class Customer
{
    private final int id;
    private int patience;
    private final LinkedQueue<Ingredient> steps;
    
    public Customer(int id, List<Ingredient> recipe, int patience)
    {
        
        if (recipe == null || recipe.isEmpty())
        {
            throw new IllegalArgumentException("Recipe must have ingredients");
            
        }
        
        if (patience <= 0)
        {
            throw new IllegalArgumentException("Patience must be positive");
        }
        
        this.id = id;
        this.patience = patience;
        steps = new LinkedQueue<>();
        for (Ingredient i : recipe)
        {
            steps.enqueue(i);
        }
    }
    
    public boolean apply(Ingredient ing)
    {
        if (ing == null)
        {
            throw new IllegalArgumentException("Ingredient cannot be null");
        }
        if (isServed() || steps.peek() != ing)
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
    
    
    
    
    
    //~ Fields ................................................................

    //~ Constructors ..........................................................

    //~Public  Methods ........................................................

}
