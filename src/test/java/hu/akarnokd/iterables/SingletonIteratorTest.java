package hu.akarnokd.iterables;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.Test;

public class SingletonIteratorTest {

    @Test
    public void contract() {
        Iterator<Integer> it = SingletonIterator.of(1);
        
        assertTrue(it.hasNext());
        
        assertEquals((Integer)1, it.next());

        assertFalse(it.hasNext());
        
        try {
            it.next();
            fail("Should have thrown");
        } catch (NoSuchElementException ex) {
            // expected
        }
    }
}
