package nl.cwts.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LargeBooleanArrayTest
{
    LargeBooleanArray a;

    @BeforeEach
    void setUp()
    {
        // Setup exactly twice the number of elements per array
        a = new LargeBooleanArray(1000);
    }

    @Test
    void get()
    {
        for (long i = 0; i < a.size(); i++)
            assertEquals(a.get(i), false);
    }

    @Test
    void set()
    {
        for (long i = 0; i < a.size(); i++)
            a.set(i, false);
    }

    @Test
    void fill()
    {
        a.fill(true);
        for (long i = 0; i < a.size(); i++)
            assertEquals(a.get(i), true);
    }

    @Test
    void append()
    {
        long s = a.size();
        a.append(true);
        assertEquals(a.size(), s + 1);
    }

    @Test
    void push()
    {
        long s = a.size();
        a.push(true);
        assertEquals(a.size(), s + 1);
    }

    @Test
    void pop()
    {
        long s = a.size();
        Boolean x = a.get(s - 1);
        Boolean y = a.pop();
        assertEquals(a.size(), s - 1);
        assertEquals(x, y);
    }

    @Test
    void clear()
    {
        a.clear();
        assertEquals(a.size(), 0);
    }

    @Test
    void ensureCapacity()
    {
        long c = a.capacity();
        a.ensureCapacity(a.capacity() + 1);
        assertEquals(a.capacity(),
                     c * (1 + LargeBooleanArray.RELATIVE_CAPACITY_INCREASE));
    }

    @Test
    void resize()
    {
        long s = a.size();
        a.resize(a.size() - 1);
        assertEquals(a.size(), s - 1);
    }

    @Test
    void shrink()
    {
        long c = a.capacity();
        a.pop();
        a.shrink();
        assertEquals(a.capacity(), a.size());
        assertEquals(a.capacity(), c - 1);
    }

    @Test
    void swap()
    {
        a.set(0, false);
        a.set(1, true);
        a.swap(0, 1);
        assertEquals(a.get(0), true);
        assertEquals(a.get(1), false);
    }

    @Test
    void mergeSort()
    {
        Random r = new Random(0);
        for (long k = 0; k < a.size(); k++)
            a.set(k, r.nextBoolean());

        // Make sure there is one identical element
        a.set(0, a.get(1));

        a.mergeSort();
        for (long i = 1; i < a.size(); i++)
            assertTrue((a.get(i) && !a.get(i - 1)) ||
                               (a.get(i) == a.get(i - 1)));
    }

    @Test
    void quickSort()
    {
        Random r = new Random(0);
        for (long k = 0; k < a.size(); k++)
            a.set(k, r.nextBoolean());

        // Make sure there is one identical element
        a.set(0, a.get(1));

        a.quickSort();
        for (long i = 1; i < a.size(); i++)
            assertTrue((a.get(i) && !a.get(i - 1)) ||
                               (a.get(i) == a.get(i - 1)));
    }

    @Test
    void sort()
    {
        Random r = new Random(0);
        for (long k = 0; k < a.size(); k++)
            a.set(k, r.nextBoolean());

        // Make sure there is one identical element
        a.set(0, a.get(1));

        a.sort();
        for (long i = 1; i < a.size(); i++)
            assertTrue((a.get(i) && !a.get(i - 1)) ||
                               (a.get(i) == a.get(i - 1)));
    }

    @Test
    void updateFrom()
    {
        Random r = new Random(0);
        LargeBooleanArray a2 = new LargeBooleanArray(a.size()/2);
        for (long i = 0; i < a2.size(); i++)
            a2.set(i, r.nextBoolean());

        long insertionPoint = a.size()/4;
        a.updateFrom(a2, 0, a2.size(), insertionPoint);

        for (long i = 0; i < a2.size(); i++)
            assertEquals(a.get(insertionPoint + i), a2.get(i));
    }

    @Test
    void copyOfRange()
    {
        Random r = new Random(0);
        for (long i = 0; i < a.size(); i++)
            a.set(i, r.nextBoolean());

        long from = (long)(0.25*a.size());
        long to = (long)(0.75*a.size());
        LargeBooleanArray a2 = a.copyOfRange(from, to);

        for (long i = 0; i < a2.size(); i++)
            assertEquals(a2.get(i), a.get(i + from));
    }

    @Test
    void toArray()
    {
        Random r = new Random(0);
        for (long i = 0; i < a.size(); i++)
            a.set(i, r.nextBoolean());

        long from = (long)(0.2*a.size());
        long to = (long)(0.4*a.size());
        boolean[] a2 = a.toArray(from, to);

        for (int i = 0; i < a2.length; i++)
            assertEquals(a2[i], a.get(i + from));
    }

    @Test
    void testClone()
    {
        LargeBooleanArray a2 = a.clone();

        for (long i = 0; i < a2.size(); i++)
            assertEquals(a2.get(i), a.get(i));
    }
}
