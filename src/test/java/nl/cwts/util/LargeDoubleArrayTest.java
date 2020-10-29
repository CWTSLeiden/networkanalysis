package nl.cwts.util;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LargeDoubleArrayTest
{
    LargeDoubleArray a;

    @BeforeEach
    void setUp()
    {
        // Setup exactly twice the number of elements per array
        a = new LargeDoubleArray(1000);
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
        double x = a.get(s - 1);
        double y = a.pop();
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
                     c * (1 + LargeDoubleArray.RELATIVE_CAPACITY_INCREASE));
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
            a.add(i, i);
            assertEquals(a.get(i), i);
        }
    }

    @Test
    void subtract()
    {
        for (long i = 0; i < a.size(); i++)
        {
            a.subtract(i, i);
            assertEquals(a.get(i), -i);
        }
    }

    @Test
    void multiply()
    {
        for (long i = 0; i < a.size(); i++)
        {
            a.add(i, 1);
            a.multiply(i, i);
            assertEquals(a.get(i), i);
        }
    }

    @Test
    void divide()
    {
        for (long i = 0; i < a.size(); i++)
        {
            a.add(i, 1);
            a.divide(i, i);
            assertEquals(a.get(i), 1.0/i);
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
            a.set(k, r.nextDouble());

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
            a.set(k, r.nextDouble());

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
            a.set(k, r.nextDouble());

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
            a.set(i, r.nextDouble());

        // Make sure there is one identical element
        a.set(0, a.get(1));

        a.sort();

        // Search first item
        double x = a.get(0);
        long j = a.binarySearch(x);
        assertEquals(a.get(j), x);

        // Search before first item
        if (a.get(0) > Double.MIN_VALUE)
        {
            x = Double.MIN_VALUE;
            j = a.binarySearch(x);
            assertEquals(j, -1);
        }

        // Search all other items
        for (long i = 1; i < n - 1; i++)
        {
            x = (a.get(i)/2 + a.get(i - 1)/2);
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
        if (a.get(n - 1) < Double.MAX_VALUE)
        {
            x = Double.MAX_VALUE;
            j = a.binarySearch(x);
            assertEquals(j, -n - 1);
        }
    }


    @Test
    void iterator()
    {
        long i = 0;
        for (double x : a)
            assertEquals(x, a.get(i++));
    }

    @Test
    void from()
    {
        long i = a.size()/2;
        for (double x : a.from(i))
            assertEquals(x, a.get(i++));
    }

    @Test
    void fromTo()
    {
        long i = a.size()/3;
        for (double x : a.fromTo(i, i + a.size()/3))
            assertEquals(x, a.get(i++));
    }

    @Test
    void updateFrom()
    {
        Random r = new Random(0);
        LargeDoubleArray a2 = new LargeDoubleArray(a.size()/2);
        for (long i = 0; i < a2.size(); i++)
            a2.set(i, r.nextDouble());

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
            a.set(i, r.nextDouble());

        long from = (long)(0.25*a.size());
        long to = (long)(0.75*a.size());
        LargeDoubleArray a2 = a.copyOfRange(from, to);

        for (long i = 0; i < a2.size(); i++)
            assertEquals(a2.get(i), a.get(i + from));
    }

    @Test
    void toArray()
    {
        Random r = new Random(0);
        for (long i = 0; i < a.size(); i++)
            a.set(i, r.nextDouble());

        long from = (long)(0.2*a.size());
        long to = (long)(0.4*a.size());
        double[] a2 = a.toArray(from, to);

        for (int i = 0; i < a2.length; i++)
            assertEquals(a2[i], a.get(i + from));
    }

    @Test
    void testClone()
    {
        LargeDoubleArray a2 = a.clone();

        for (long i = 0; i < a2.size(); i++)
            assertEquals(a2.get(i), a.get(i));
    }
}
