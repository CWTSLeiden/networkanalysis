package nl.cwts.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LargeIntArrayTest
{
    LargeIntArray a;

    @BeforeEach
    void setUp()
    {
        // Setup exactly twice the number of elements per array
        a = new LargeIntArray(1000);
    }

    @Test
    void get()
    {
        for (long i = 0; i < a.size(); i++)
            assertEquals(a.get(i), 0);
    }

    @Test
    void set()
    {
        for (long i = 0; i < a.size(); i++)
            a.set(i, 0);
    }

    @Test
    void fill()
    {
        a.fill(1);
        for (long i = 0; i < a.size(); i++)
            assertEquals(a.get(i), 1);
    }

    @Test
    void append()
    {
        long s = a.size();
        a.append(0);
        assertEquals(a.size(), s + 1);
    }

    @Test
    void push()
    {
        long s = a.size();
        a.push(0);
        assertEquals(a.size(), s + 1);
    }

    @Test
    void pop()
    {
        long s = a.size();
        int x = a.get(s - 1);
        int y = a.pop();
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
                     c * (1 + LargeIntArray.RELATIVE_CAPACITY_INCREASE));
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
    void add()
    {
        for (long i = 0; i < a.size(); i++)
        {
            a.add(i, (int)i);
            assertEquals(a.get(i), i);
        }
    }

    @Test
    void subtract()
    {
        for (long i = 0; i < a.size(); i++)
        {
            a.subtract(i, (int)i);
            assertEquals(a.get(i), -(int)i);
        }
    }

    @Test
    void multiply()
    {
        for (long i = 0; i < a.size(); i++)
        {
            a.add(i, 1);
            a.multiply(i, (int)i);
            assertEquals(a.get(i), (int)i);
        }
    }

    @Test
    void divide()
    {
        for (long i = 0; i < a.size(); i++)
        {
            a.add(i, (int)i + 1);
            a.divide(i, (int)i + 1);
            assertEquals(a.get(i), 1);
        }
    }

    @Test
    void calcSum()
    {
        assertEquals(a.calcSum(), 0);
    }

    @Test
    void calcAverage()
    {
        assertEquals(a.calcAverage(), 0);
    }

    @Test
    void calcMaximum()
    {
        assertEquals(a.calcMaximum(), 0);
    }

    @Test
    void calcMinimum()
    {
        assertEquals(a.calcMinimum(), 0);
    }

    @Test
    void swap()
    {
        a.set(0,42);
        a.set(1, 59);
        a.swap(0, 1);
        assertEquals(a.get(0), 59);
        assertEquals(a.get(1), 42);
    }

    @Test
    void mergeSort()
    {
        Random r = new Random(0);
        for (long k = 0; k < a.size(); k++)
            a.set(k, r.nextInt());

        // Make sure there is one identical element
        a.set(0, a.get(1));

        a.mergeSort();
        for (long i = 1; i < a.size(); i++)
            assertTrue(a.get(i) >= a.get(i - 1));
    }

    @Test
    void quickSort()
    {
        Random r = new Random(0);
        for (long k = 0; k < a.size(); k++)
            a.set(k, r.nextInt());

        // Make sure there is one identical element
        a.set(0, a.get(1));

        a.quickSort();
        for (long i = 1; i < a.size(); i++)
            assertTrue(a.get(i) >= a.get(i - 1));
    }

    @Test
    void sort()
    {
        Random r = new Random(0);
        for (long k = 0; k < a.size(); k++)
            a.set(k, r.nextInt());

        // Make sure there is one identical element
        a.set(0, a.get(1));

        a.sort();
        for (long i = 1; i < a.size(); i++)
            assertTrue(a.get(i) >= a.get(i - 1));
    }

    @Test
    void binarySearch()
    {
        Random r = new Random(0);
        long n = a.size();

        for (long i = 0; i < a.size(); i++)
            a.set(i, r.nextInt());

        // Make sure there is one identical element
        a.set(0, a.get(1));

        a.sort();

        // Search first item
        int x = a.get(0);
        long j = a.binarySearch(x);
        assertEquals(a.get(j), x);

        // Search before first item
        if (a.get(0) > Integer.MIN_VALUE)
        {
            x = Integer.MIN_VALUE;
            j = a.binarySearch(x);
            assertEquals(j, -1);
        }

        // Search all other items
        for (long i = 1; i < n - 1; i++)
        {
            x = (int)(a.get(i)/2.0 + a.get(i - 1)/2.0);
            j = a.binarySearch(x);

            if (j < 0)
            {
                long insertionPoint = -j - 1;
                assertTrue(x < a.get(insertionPoint));
                if (insertionPoint > 0)
                    assertTrue(x >= a.get(insertionPoint - 1));
            }
            else
                assertEquals(a.get(j), x);

            x = a.get(i);
            j = a.binarySearch(x);
            assertTrue(j > 0);
            assertEquals(a.get(j), x);
        }

        // Search last item
        x = a.get(n - 1);
        j = a.binarySearch(x);
        assertEquals(a.get(j), x);

        // Search after last item
        if (a.get(n - 1) < Integer.MAX_VALUE)
        {
            x = Integer.MAX_VALUE;
            j = a.binarySearch(x);
            assertEquals(j, -n - 1);
        }
    }


    @Test
    void iterator()
    {
        long i = 0;
        for (int x : a)
            assertEquals(x, a.get(i++));
    }

    @Test
    void from()
    {
        long i = a.size()/2;
        for (int x : a.from(i))
            assertEquals(x, a.get(i++));
    }

    @Test
    void fromTo()
    {
        long i = a.size()/3;
        for (int x : a.fromTo(i, i + a.size()/3))
            assertEquals(x, a.get(i++));
    }

    @Test
    void updateFrom()
    {
        Random r = new Random(0);
        LargeIntArray a2 = new LargeIntArray(a.size()/2);
        for (long i = 0; i < a2.size(); i++)
            a2.set(i, r.nextInt());

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
            a.set(i, r.nextInt());

        long from = (long)(0.25*a.size());
        long to = (long)(0.75*a.size());
        LargeIntArray a2 = a.copyOfRange(from, to);

        for (long i = 0; i < a2.size(); i++)
            assertEquals(a2.get(i), a.get(i + from));
    }

    @Test
    void toArray()
    {
        Random r = new Random(0);
        for (long i = 0; i < a.size(); i++)
            a.set(i, r.nextInt());

        long from = (long)(0.2*a.size());
        long to = (long)(0.4*a.size());
        int[] a2 = a.toArray(from, to);

        for (int i = 0; i < a2.length; i++)
            assertEquals(a2[i], a.get(i + from));
    }

    @Test
    void testClone()
    {
        LargeIntArray a2 = a.clone();

        for (long i = 0; i < a2.size(); i++)
            assertEquals(a2.get(i), a.get(i));
    }
}
