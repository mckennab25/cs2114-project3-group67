package krustykrab;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class LinkedQueue<T>
{
    //~ Fields ................................................................
    private Node<T> f;
    private Node<T> b;
    private int size;
    
    //~ Constructors ..........................................................
    
    public void enqueue(T entry)
    {
        if (entry == null)
        {
            throw new IllegalArgumentException("Queue entry cannot be null");
        }
        
        
        Node<T> node = new Node<>(entry);
        
        
        if (b == null)
        {
            f = node;
        }
        else
        {
            b.next = node;
        }
        
        b = node;
        size++;
    }
    
    
    public T dequeue()
    {
        T entry = peek();
        f = f.next;
        size--;

        if (f == null)
        {
            b = null;
        }

        return entry;
    }
    public T peek()
    {
        if (isEmpty())
        {
            throw new NoSuchElementException("Queue is empty");
        }

        return f.entry;
    }
    
    public boolean isEmpty()
    {
        return size == 0;
    }

    public int getSize()
    {
        return size;
    }
    
    List<T> snapshot()
    {
        List<T> entries = new ArrayList<>(size);
        for (Node<T> node = f; node != null; node = node.next)
        {
            entries.add(node.entry);
        }
        return entries;
    }

    private static class Node<T>
    {
        private final T entry;
        private Node<T> next;

        Node(T entry)
        {
            this.entry = entry;
        }
    }
    
    
    
    
    
    
    
    
    
    //~Public  Methods ........................................................

}
