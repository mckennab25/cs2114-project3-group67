package krustykrab;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.Test;

/** Tests the queue, customer, and ingredient types. */
public class ConnorsTest
{
    private static final List<Ingredient> RECIPE = Arrays.asList(
        Ingredient.BOTTOM_BUN, Ingredient.PATTY, Ingredient.CHEESE,
        Ingredient.LETTUCE, Ingredient.TOMATO, Ingredient.TOP_BUN);

    @Test
    public void queuePreservesOrderAndCanBeReused()
    {
        LinkedQueue<String> queue = new LinkedQueue<>();
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.getSize());

        queue.enqueue("A");
        queue.enqueue("B");
        assertFalse(queue.isEmpty());
        assertEquals(2, queue.getSize());
        assertEquals("A", queue.peek());
        assertEquals(2, queue.getSize());
        assertEquals("A", queue.dequeue());
        assertEquals("B", queue.peek());
        assertEquals("B", queue.dequeue());
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.getSize());

        queue.enqueue("C");
        assertEquals("C", queue.dequeue());
    }

    @Test
    public void queueRejectsInvalidOperations()
    {
        LinkedQueue<String> queue = new LinkedQueue<>();
        assertThrows(NoSuchElementException.class, queue::peek);
        assertThrows(NoSuchElementException.class, queue::dequeue);
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.getSize());

        queue.enqueue("A");
        assertThrows(IllegalArgumentException.class, () -> queue.enqueue(null));
        assertEquals(1, queue.getSize());
        assertEquals("A", queue.peek());
    }

    @Test
    public void customerCopiesRecipeAndRemainingSteps()
    {
        List<Ingredient> recipe = new ArrayList<>(RECIPE);
        Customer customer = new Customer(7, recipe, 20);
        recipe.clear();
        customer.getSteps().clear();

        assertEquals(7, customer.getId());
        assertEquals(20, customer.getPatience());
        assertEquals(RECIPE, customer.getSteps());
        assertFalse(customer.isServed());
        assertFalse(customer.hasWalkedOut());
    }

    @Test
    public void customerRejectsInvalidConstructorArguments()
    {
        assertThrows(IllegalArgumentException.class,
            () -> new Customer(1, null, 20));
        assertThrows(IllegalArgumentException.class,
            () -> new Customer(1, Collections.emptyList(), 20));
        assertThrows(IllegalArgumentException.class,
            () -> new Customer(1, RECIPE, 0));
        assertThrows(IllegalArgumentException.class,
            () -> new Customer(1, RECIPE, -1));
        assertThrows(IllegalArgumentException.class,
            () -> new Customer(1, Collections.singletonList(null), 20));
    }

    @Test
    public void customerAcceptsIngredientsInOrder()
    {
        Customer customer = new Customer(1, RECIPE, 20);
        assertFalse(customer.apply(Ingredient.PATTY));
        assertThrows(IllegalArgumentException.class, () -> customer.apply(null));
        assertEquals(RECIPE, customer.getSteps());

        assertTrue(customer.apply(Ingredient.BOTTOM_BUN));
        assertEquals(RECIPE.subList(1, RECIPE.size()), customer.getSteps());
        assertFalse(customer.isServed());
        for (Ingredient ingredient : RECIPE.subList(1, RECIPE.size()))
        {
            assertTrue(customer.apply(ingredient));
        }

        assertTrue(customer.isServed());
        assertTrue(customer.getSteps().isEmpty());
        assertFalse(customer.apply(Ingredient.TOP_BUN));
    }

    @Test
    public void customerWalksOutAtZeroPatience()
    {
        Customer customer = new Customer(1, RECIPE, 2);
        customer.losePatience();
        assertEquals(1, customer.getPatience());
        assertFalse(customer.hasWalkedOut());

        customer.losePatience();
        assertEquals(0, customer.getPatience());
        assertTrue(customer.hasWalkedOut());

        customer.losePatience();
        assertEquals(0, customer.getPatience());
    }

    @Test
    public void ingredientsFollowRecipeOrder()
    {
        assertArrayEquals(RECIPE.toArray(new Ingredient[0]), Ingredient.values());
    }
}
