/**
 * 
 */
package org.mmocore.util.collections.concurrent;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This a semi-concurrent linked list implementation that aims to provide 
 * a high speed solution that fullfill the most used pattern of the MMOCore.<BR>
 * 
 * Iterations forward only
 * 1 thread appends (N if synchronized)
 * 1 thread read/delete
 * 
 * @author KenM
 *
 */
public class SemiConcurrentLinkedList<E> implements Iterable<E>
{
    private volatile Node<E> _start = new Node<E>();
    private volatile Node<E> _end = new Node<E>();
    private int _size;
    
    public SemiConcurrentLinkedList()
    {
        this.clear();
    }
    
    public void addLast(E elem)
    {
        Node<E> oldEndNode = _end;
        
        // assign the value to the current last node
        oldEndNode._value = elem;
        
        // create the new end node
        Node<E> newEndNode = new Node<E>(oldEndNode, null, null);
        
        // assign the new end as the next of the current end
        oldEndNode.setNext(newEndNode);
        
        // place the new end
        _end = newEndNode;
        
        // increment list size
        _size++;
    }
    
    public void remove(Node<E> node)
    {
        Node<E> previous = node.getPrevious();
        Node<E> next = node.getNext();
        
        // set the next to skip the node being deleted
        previous.setNext(next);
        
        // adjust previous
        next.setPrevious(previous);
        
        _size--;
    }
    
    public int size()
    {
        return _size;
    }
    
    public final boolean isEmpty()
    {
        return _size == 0;
    }
    
    public void clear()
    {
        _start.setNext(_end);
        _end.setPrevious(_start);
    }
    
    public Node<E> getStart()
    {
        return _start;
    }
    
    public Node<E> getEnd()
    {
        return _end;
    }
    
    public Iterator<E> iterator()
    {
        return new SemiConcurrentIterator(_start);
    }
    
    public class SemiConcurrentIterator implements Iterator<E>
    {
        private Node<E> _current;
        //private Node<V> _last;
        
        protected SemiConcurrentIterator(Node<E> start)
        {
            _current = start;
        }
        
        public boolean hasNext()
        {
            return _current.getNext() != _end;
        }

        public E next()
        {
            _current = _current.getNext();
            if (_current == _end)
            {
                throw new NoSuchElementException();
            }
            return _current.getValue();
        }

        public void remove()
        {
            SemiConcurrentLinkedList.this.remove(_current);
            _current = _current.getPrevious();
        }
    }
    
    public final class Node<T>
    {
        private Node<T> _previous;
        private Node<T> _next;
        private T _value;
        
        protected Node()
        {
        }
        
        protected Node(T value)
        {
            _value = value;
        }
        
        protected Node(Node<T> previous, Node<T> next, T value)
        {
            _previous = previous;
            _next = next;
            _value = value;
        }
        
        public Node<T> getNext()
        {
            return _next;
        }
        
        protected void setNext(Node<T> node)
        {
            _next = node;
        }
        
        public Node<T> getPrevious()
        {
            return _previous;
        }
        
        protected void setPrevious(Node<T> node)
        {
            _previous = node;
        }
        
        public T getValue()
        {
            return _value;
        }
    }
}
