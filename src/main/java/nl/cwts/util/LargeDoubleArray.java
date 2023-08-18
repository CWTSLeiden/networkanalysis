package nl.cwts.util;

/* We need the fastutil package for sorting purposes */

import it.unimi.dsi.fastutil.BigArrays;
import it.unimi.dsi.fastutil.longs.LongComparator;

import java.util.Arrays;
import java.util.PrimitiveIterator;

/**
 * <p>
 * This class enables arrays of doubles up to 64-bits in size (for the exact
 * maximum size, please see {@link #MAX_SIZE}). As a single array is limited to
 * 32-bits in Java, this is done by having an array of arrays. We use a
 * {@code long} to index this array of arrays, and use bitwise operators
 * to extract the two indices for the array of arrays. The largest index is
 * extracted using {@link #getSegment} while the smallest index is extracted
 * using {@link #getOffset}, where the actual value is located in
 * {@code values[segment][offset]}.
 * </p>
 *
 * <p>
 * In addition, this class enables a dynamic array, and can be dynamically
 * enlarged or shrunken. The total capacity is indicated by {@link #capacity()},
 * while the actual number of elements is indicated by {@link #size()}. When
 * constructing new arrays, typically the size is set equal to the capacity,
 * with the restriction that we always at least reserve a minimum capacity
 * (indicated by {@link #MINIMUM_INITIAL_CAPACITY}). This dynamic array then
 * also supports {@link #push} and {@link #pop} operations.
 * </p>
 *
 * <p>
 * This class also contains a number of convenience functions to easily {@link
 * #add}, {@link #subtract}, {@link #multiply} and {@link #divide} elements.
 * This is more efficient than calling both {@code set} and
 * {@code get} because this requires to retrieve indices twice.
 * </p>
 *
 * <p>
 * To facilitate easy iteration over this array, a few convenience iterables are
 * provided, enabling you to use construct such as
 * {@code for (double x : array)} and
 * {@code for (double x : array.fromto(10, 50)}
 * </p>
 */
public final class LargeDoubleArray implements Cloneable, Iterable<Double>
{
    /**
     * The number of bits to use for each individual array
     */
    public static final byte ARRAY_BIT_SIZE = 30;
    /**
     * The maximum size of each individual array
     */
    public static final int MAX_SIZE_ARRAY = 1 << ARRAY_BIT_SIZE;
    /**
     * Minimum initial capacity of the array.
     */
    public static final long MINIMUM_INITIAL_CAPACITY = 10;
    /**
     * The relative increase in the capacity when increasing the capacity.
     */
    public static final double RELATIVE_CAPACITY_INCREASE = 0.5;
    /**
     * The maximum size of the array in total.
     */
    public static final long MAX_SIZE = ((1L << 31) - 5) * MAX_SIZE_ARRAY;
    /**
     * The array mask that is used to obtain the proper lower index.
     */
    private static final long ARRAY_MASK = MAX_SIZE_ARRAY - 1;
    /**
     * The current total capacity of the array.
     */
    private long capacity;

    /**
     * The current total size of the array.
     */
    private long size;

    /**
     * The actual array of arrays that is used as storage.
     */
    private double[][] values;

    /***************************************************************************
     * Constructors
     **************************************************************************/

    /**
     * Constructs a new empty array of specified size.
     *
     * <p>
     * Both the capacity and the size is being set to {@code size}, with
     * all elements initialised to their default value. If instead, you prefer
     * an empty array, but reserve capacity, pass {@code 0}
     * {@code size} here and use {@link #ensureCapacity(long)}.
     * </p>
     *
     * @param size Size of the array
     */
    public LargeDoubleArray(long size)
    {
        int nSegments, segment;
        long remainingLength;

        if (size < 0)
            throw new IllegalArgumentException("Size cannot be negative.");

        this.capacity = Math.max(size, MINIMUM_INITIAL_CAPACITY);
        this.size = size;

        // Construct number of arrays that are needed
        nSegments = getSegment(capacity);
        if (getOffset(capacity) > 0)
            nSegments += 1; // Add one if there was a remainder
        this.values = new double[nSegments][];

        // Create each separate array
        remainingLength = capacity;
        segment = 0;
        while (remainingLength > MAX_SIZE_ARRAY)
        {
            // As long as longer than a single array, we allocate
            // up until MAX_SIZE_ARRAY
            this.values[segment] = new double[MAX_SIZE_ARRAY];
            remainingLength -= MAX_SIZE_ARRAY;
            segment++;
        }

        // We now allocate the last part
        if (remainingLength > 0)
            this.values[segment] = new double[(int)remainingLength];
    }

    /**
     * Constructs a new array of indicated size with all elements set to a
     * constant value.
     *
     * @param size     Size of the array
     * @param constant Constant value to set
     */
    public LargeDoubleArray(long size, double constant)
    {
        this(size);

        this.fill(constant);
    }

    /**
     * Constructs a new array copying the values of the supplied array.
     *
     * @param values Array to copy from
     */
    public LargeDoubleArray(double[] values)
    {
        this(values.length);

        int offset, segment;

        offset = 0;
        segment = 0;

        for (double x : values)
        {
            if (offset >= MAX_SIZE_ARRAY)
            {
                offset = 0;
                segment++;
            }
            this.values[segment][offset] = x;
            offset++;
        }
    }

    /***************************************************************************
     * Getting / setting elements and appropriate indices
     **************************************************************************/

    /**
     * Gets the segment of the index.
     *
     * @param index Index
     *
     * @return Segment
     */
    public static int getSegment(long index)
    {
        return (int)(index >>> ARRAY_BIT_SIZE);
    }

    /**
     * Gets the offset within a particular segment of the index.
     *
     * @param index Index
     *
     * @return Offset within particular segment
     */
    public static int getOffset(long index)
    {
        return (int)(index & ARRAY_MASK);
    }

    /**
     * Gets the number of segments.
     *
     * @return Number of segments
     */
    public int nSegments()
    {
        return values.length;
    }

    /**
     * Gets the length of the indicated segment.
     *
     * @param segment Segment
     *
     * @return Length of segment
     */
    public int length(int segment)
    {
        return this.values[segment].length;
    }

    /**
     * Gets value element based on long index.
     *
     * @param index Index of element
     *
     * @return Value of element
     */
    public double get(long index)
    {
        return this.values[getSegment(index)][getOffset(index)];
    }

    /**
     * Gets value element based on segment and offset.
     *
     * @param segment Segment of element
     * @param offset  Offset of element
     *
     * @return Value of element
     */
    public double get(int segment, int offset)
    {
        return this.values[segment][offset];
    }

    /**
     * Sets element to value for indicated index
     *
     * @param index Index of element
     * @param value Value
     */
    public void set(long index, double value)
    {
        this.values[getSegment(index)][getOffset(index)] = value;
    }

    /**
     * Sets element to value for indicated segment and offset.
     *
     * @param segment Segment of element
     * @param offset  Offset of element
     * @param value   Value
     */
    public void set(int segment, int offset, double value)
    {
        this.values[segment][offset] = value;
    }

    /**
     * Fills entire array with constant.
     *
     * @param constant Constant
     */
    public void fill(double constant)
    {
        fill(0, this.size, constant);
    }

    /**
     * Fills indicated range with constant.
     *
     * @param from     From index, inclusive
     * @param to       To index, exclusive
     * @param constant Constant
     */
    public void fill(long from, long to, double constant)
    {
        // Determine initial indices for this array
        int segmentTo, offsetTo, segment;

        segmentTo = getSegment(to);
        offsetTo = getOffset(to);

        // Fill first segment
        segment = getSegment(from);
        Arrays.fill(this.values[segment], getOffset(from),
                    segment == segmentTo ? getOffset(to) : this.values[segment].length, constant);
        segment++;

        // Fill subsequent segments
        for (; segment < segmentTo; segment++)
            Arrays.fill(this.values[segment], 0, this.values[segment].length, constant);

        // Fill last segment
        if (segment == segmentTo && offsetTo > 0)
            Arrays.fill(this.values[segment], 0,
                        offsetTo, constant);
    }

    /***************************************************************************
     * Dynamic array functions
     **************************************************************************/

    /**
     * Appends a specified value to the end of the array.
     *
     * @param value Value
     *
     * @see #push
     * @see #pop
     */
    public void append(double value)
    {
        ensureCapacity(size + 1);
        set(size, value);
        size++;
    }

    /**
     * Appends a specified value to the end of the array.
     *
     * @param value Value
     *
     * @see #append
     * @see #pop
     */
    public void push(double value)
    {
        append(value);
    }

    /**
     * Pops last value off the end of the array.
     *
     * @return Last value
     */
    public double pop()
    {
        double value;
        value = get(size - 1);
        size--;
        return value;
    }

    /**
     * Removes all elements from the array.
     */
    public void clear()
    {
        size = 0;
    }

    /**
     * Increases the capacity to ensure that the array has at least the minimum
     * capacity.
     *
     * <p>
     * The capacity is increased with a percentage indicated by {@link
     * #RELATIVE_CAPACITY_INCREASE}, unless the indicated minimum capacity
     * exceeds this.
     * </p>
     *
     * @param minCapacity Minimum capacity
     */
    public void ensureCapacity(long minCapacity)
    {
        double[][] newValues;
        int nOldSegments, nNewSegments, segment;
        long newCapacity, oldCapacity, remainingLength;

        oldCapacity = capacity;
        if (minCapacity > oldCapacity)
        {
            newCapacity =
                    (long)((1 + RELATIVE_CAPACITY_INCREASE) * oldCapacity);
            if (newCapacity < minCapacity)
                newCapacity = minCapacity;

            // Determine the number of new segments that we will need to
            // allocate
            nNewSegments = getSegment(newCapacity);
            if (getOffset(newCapacity) > 0)
                nNewSegments += 1; // Add one if there was a remainder
            newValues = new double[nNewSegments][];

            // Determine which array we need to increase
            nOldSegments = getSegment(oldCapacity);
            if (getOffset(oldCapacity) > 0)
                nOldSegments += 1; // Add one if there was a remainder

            // Simply refer to the previously existing segments for all
            // segments except for the last one.
            remainingLength = newCapacity;
            for (segment = 0; segment < nOldSegments - 1; segment++)
            {
                newValues[segment] = values[segment];
                remainingLength -= values[segment].length;
            }

            // We now need to copy only the last old segment
            if (remainingLength > MAX_SIZE_ARRAY)
                newValues[segment] = Arrays.copyOf(values[segment],
                                                   MAX_SIZE_ARRAY);
            else
                newValues[segment] = Arrays.copyOf(values[segment],
                                                   (int)remainingLength);
            remainingLength -= newValues[segment].length;
            segment++;

            // For the remaining segments, we can simply allocate new arrays
            while (remainingLength > MAX_SIZE_ARRAY)
            {
                // As long as longer than a single array, we allocate up until
                // MAX_SIZE_ARRAY
                newValues[segment] = new double[MAX_SIZE_ARRAY];

                remainingLength -= MAX_SIZE_ARRAY;
                segment++;
            }

            // Allocate the last part
            if (remainingLength > 0)
                newValues[segment] = new double[(int)remainingLength];

            // Assign to current values
            this.values = newValues;
            this.capacity = newCapacity;
        }
    }

    /**
     * Resizes array.
     * @param size New size
     */
    public void resize(long size)
    {
        this.size = size;
    }

    /**
     * Shrinks capacity to fit actual size.
     */
    public void shrink()
    {
        double[][] newValues;
        int segment, nNewSegments;
        long newCapacity, oldCapacity, remainingLength;

        newCapacity = size;
        oldCapacity = capacity;

        if (newCapacity < oldCapacity)
        {
            // Determine the number of new segments
            nNewSegments = getSegment(newCapacity);
            if (getOffset(newCapacity) > 0)
                nNewSegments += 1; // Add one if there was a remainder
            newValues = new double[nNewSegments][];

            // Simply refer to the previously existing arrays for the first
            // couple of arrays
            remainingLength = newCapacity;
            for (segment = 0; segment < nNewSegments - 1; segment++)
            {
                newValues[segment] = values[segment];
                remainingLength -= newValues[segment].length;
            }

            if (remainingLength > MAX_SIZE_ARRAY)
                // This should not be possible
                throw new IndexOutOfBoundsException("Error while shrinking array to fit capacity to actual size.");

            // Truncate the last segment to the new capacity
            if (remainingLength > 0)
                newValues[segment] = Arrays.copyOf(values[segment],
                                                   (int)remainingLength);

            // Assign to actual values
            this.values = newValues;
            this.capacity = newCapacity;
        }
    }

    /**
     * Gets size of array.
     * <p>This is always less than or equal to the capacity.</p>
     *
     * @return Size
     */
    public long size()
    {
        return size;
    }

    /**
     * Gets capacity of array.
     * <p>This is always greater than or equal to the size.</p>
     *
     * @return Capacity
     */
    public long capacity()
    {
        return capacity;
    }

    /***************************************************************************
     * Helper functions
     **************************************************************************/

    /**
     * Adds addition to existing element.
     *
     * @param index    Index of element
     * @param addition Value to add
     */
    public void add(long index, double addition)
    {
        this.values[getSegment(index)][getOffset(index)] += addition;
    }

    /**
     * Subtracts subtraction from existing element.
     *
     * @param index       Index of element
     * @param subtraction Value to subtract
     */
    public void subtract(long index, double subtraction)
    {
        this.values[getSegment(index)][getOffset(index)] -= subtraction;
    }

    /**
     * Multiplies existing element by multiplier.
     *
     * @param index      Index of element
     * @param multiplier Multiplier
     */
    public void multiply(long index, double multiplier)
    {
        this.values[getSegment(index)][getOffset(index)] *= multiplier;
    }

    /**
     * Divides existing element by divisor.
     *
     * @param index   Index of element
     * @param divisor Divisor
     */
    public void divide(long index, double divisor)
    {
        this.values[getSegment(index)][getOffset(index)] /= divisor;
    }


    /**
     * Calculates the sum of all elements.
     *
     * @return Sum
     */
    public double calcSum()
    {
        return calcSum(0, size);
    }

    /**
     * Calculates the sum of all elements in the indicated range.
     *
     * @param from From index, inclusive
     * @param to   To index, exclusive
     *
     * @return Sum
     */
    public double calcSum(long from, long to)
    {
        double sum;
        int segment, offset;
        long i;

        sum = 0.0;
        segment = getSegment(from);
        offset = getOffset(from);

        for (i = from; i < to; i++)
        {
            if (offset >= MAX_SIZE_ARRAY)
            {
                offset = 0;
                segment++;
            }

            sum += values[segment][offset];

            offset++;
        }
        return sum;
    }

    /**
     * Calculates the average of all elements.
     *
     * @return Average
     */
    public double calcAverage()
    {
        return calcSum() / this.capacity;
    }

    /**
     * Calculates the maximum of all elements.
     *
     * @return Maximum
     */
    public double calcMaximum()
    {
        double max;
        int offset, segment;
        long i;

        max = Double.NEGATIVE_INFINITY;
        offset = 0;
        segment = 0;

        for (i = 0; i < size; i++)
        {
            if (offset >= MAX_SIZE_ARRAY)
            {
                offset = 0;
                segment++;
            }
            if (this.values[segment][offset] > max)
                max = this.values[segment][offset];
            offset++;
        }
        return max;
    }

    /**
     * Calculates the minimum of all elements.
     *
     * @return Minimum
     */
    public double calcMinimum()
    {
        double min;
        int offset, segment;
        long i;

        min = Double.POSITIVE_INFINITY;
        offset = 0;
        segment = 0;

        for (i = 0; i < size; i++)
        {
            if (offset >= MAX_SIZE_ARRAY)
            {
                offset = 0;
                segment++;
            }
            if (this.values[segment][offset] < min)
                min = this.values[segment][offset];
            offset++;
        }
        return min;
    }

    /**
     * Swaps two elements.
     *
     * @param indexA First element to swap
     * @param indexB Second element to swap
     */
    public void swap(long indexA, long indexB)
    {
        double tmp;
        int segmentA, offsetA, segmentB, offsetB;

        segmentA = getSegment(indexA);
        offsetA = getOffset(indexA);

        segmentB = getSegment(indexB);
        offsetB = getOffset(indexB);

        tmp = values[segmentA][offsetA];
        values[segmentA][offsetA] = values[segmentB][offsetB];
        values[segmentB][offsetB] = tmp;
    }

    /**
     * Compares two elements.
     *
     * @param indexA First element to compare
     * @param indexB Second element to compare
     *
     * @return Integer indicating which element is greater. In particular, it
     * returns -1 if the first is less than the second, +1 if the first is
     * greater than the second, and 0 if both are equal.
     */
    private int compare(long indexA, long indexB)
    {
        return Double.compare(get(indexA), get(indexB));
    }

    /***************************************************************************
     * Sorting and Binary Search
     **************************************************************************/

    /**
     * Sorts elements in ascending order using merge sort.
     */
    public void mergeSort()
    {
        BigArrays.mergeSort(0, capacity,
                            this::compare,
                            this::swap);
    }

    /**
     * Sorts elements in ascending order using quick sort.
     */
    public void quickSort()
    {
        BigArrays.quickSort(0, capacity,
                            this::compare,
                            this::swap);
    }

    /**
     * Sorts elements in ascending order using merge sort.
     */
    public void sort()
    {
        mergeSort();
    }

    /**
     * Sorts elements in a specified order using merge sort.
     *
     * @param comparator Comparator indicating the desired ordering
     */
    public void sort(LongComparator comparator)
    {
        BigArrays.mergeSort(0, size, comparator, this::swap);
    }

    /**
     * Searches for the specified value using binary search.
     *
     * <p>
     * The array must be sorted in ascending order prior to making this call. If
     * it is not sorted, the results are undefined. If the array contains
     * multiple elements with the specified value, there is no guarantee which
     * one will be found.
     * <p>
     * Parameters: a - the array to be searched key - the value to be searched
     * for Returns:
     * </p>
     *
     * @param value The value to search for
     *
     * @return index of the search key, if it is contained in the array;
     * otherwise, (-(insertion point) - 1). The insertion point is defined as
     * the point at which the key would be inserted into the array: the index of
     * the first element greater than the key, or the total size if all elements
     * in the array are less than the specified key. Note that this guarantees
     * that the return value will be non-negative if and only if the key is
     * found.
     */
    public long binarySearch(double value)
    {
        return binarySearch(0, size, value);
    }

    /**
     * Searches for the specified value using binary search in the indicated
     * range.
     *
     * <p>
     * The (range of the) array must be sorted in ascending order prior to
     * making this call. If it is not sorted, the results are undefined. If the
     * array contains multiple elements with the specified value, there is no
     * guarantee which one will be found.
     * <p>
     * Parameters: a - the array to be searched key - the value to be searched
     * for Returns:
     * </p>
     *
     * @param from  From index, inclusive
     * @param to    To index, exclusive
     * @param value The value to search for
     *
     * @return index of the search key, if it is contained in the array;
     * otherwise, (-(insertion point) - 1). The insertion point is defined as
     * the point at which the key would be inserted into the array: the index of
     * the first element greater than the key, or the total size if all elements
     * in the array are less than the specified key. Note that this guarantees
     * that the return value will be non-negative if and only if the key is
     * found.
     */
    public long binarySearch(long from, long to, double value)
    {
        long minIdx, maxIdx, midIdx;

        minIdx = from;
        maxIdx = to - 1;
        midIdx = -1;

        if (value < get(minIdx))
            return -1;

        if (value > get(maxIdx))
            return -to - 1;

        while (minIdx < maxIdx - 1)
        {
            midIdx = (minIdx + maxIdx) / 2;
            if (get(midIdx) > value)
                maxIdx = midIdx;
            else
                minIdx = midIdx;
        }
        // We now know that get(minIdx) <= value <= get(maxIdx) so check bounds
        if (value == get(minIdx))
            return minIdx;
        else if (value == get(maxIdx))
            return maxIdx;
        else
            return -maxIdx - 1;
    }

    /***************************************************************************
     * Iterators
     **************************************************************************/

    /**
     * Iterates over all elements
     *
     * @return Iterator
     */
    @Override
    public Iterator iterator()
    {
        return new Iterator();
    }

    /**
     * Iterates over all elements, starting from the indicated element.
     *
     * @param from From index, inclusive
     *
     * @return Iterable
     */
    public FromIterable from(long from)
    {
        return new FromIterable(from);
    }

    /**
     * Iterates over elements in indicated range.
     *
     * @param from From index, inclusive
     * @param to   To index, exclusive
     *
     * @return Iterable
     */
    public FromToIterable fromTo(long from, long to)
    {
        return new FromToIterable(from, to);
    }

    /**
     * Updates this array from the provided array.
     *
     * @param array Array to update from
     */
    public void updateFrom(LargeDoubleArray array)
    {
        updateFrom(array, 0, array.size(), 0);
    }

    /**
     * Updates this array from the provided array.
     * <p>
     * Values from other array starting at {@code from} until
     * {@code to} (exclusive) will be copied to this array, starting
     * from the {@code insertionPoint} onwards.
     *
     * @param array          Array to update from
     * @param from           Index in {@code array} from where to update,
     *                       inclusive
     * @param to             Index in {@code array} until where to update,
     *                       exclusive
     * @param insertionPoint Starting index in this array to update
     */
    public void updateFrom(LargeDoubleArray array, long from, long to, long insertionPoint)
    {
        long length, i;
        int segmentFrom, offsetFrom, segment, offset;

        length = to - from;
        // Determine initial indices for this array
        segmentFrom = getSegment(from);
        offsetFrom = getOffset(from);

        // Determine initial indices for new array
        segment = getSegment(insertionPoint);
        offset = getOffset(insertionPoint);

        for (i = 0; i < length; i++)
        {
            if (offsetFrom >= MAX_SIZE_ARRAY)
            {
                offsetFrom = 0;
                segmentFrom++;
            }
            if (offset >= MAX_SIZE_ARRAY)
            {
                offset = 0;
                segment++;
            }

            // Copy actual value
            this.values[segment][offset] =
                    array.values[segmentFrom][offsetFrom];

            offsetFrom++;
            offset++;
        }
    }

    /**
     * Copies the specified range to a new array.
     *
     * @param from From index, inclusive
     * @param to   To index, exclusive
     * @return New array
     */
    public LargeDoubleArray copyOfRange(long from, long to)
    {
        long lengthNew, i;
        int segmentOrig, offsetOrig, segmentNew, offsetNew;
        LargeDoubleArray copy;

        lengthNew = to - from;
        copy = new LargeDoubleArray(lengthNew);

        // Determine initial indices for this array
        segmentOrig = getSegment(from);
        offsetOrig = getOffset(from);

        // Determine initial indices for new array
        segmentNew = 0;
        offsetNew = 0;

        for (i = 0; i < lengthNew; i++)
        {
            if (offsetOrig >= MAX_SIZE_ARRAY)
            {
                offsetOrig = 0;
                segmentOrig++;
            }
            if (offsetNew >= MAX_SIZE_ARRAY)
            {
                offsetNew = 0;
                segmentNew++;
            }

            // Copy actual value
            copy.values[segmentNew][offsetNew] = this.values[segmentOrig][offsetOrig];

            offsetOrig++;
            offsetNew++;
        }
        return copy;
    }

    /***************************************************************************
     * Copying, cloning and updating
     **************************************************************************/

    /**
     * Copies the values to an ordinary array.
     *
     * <p>
     * Not all elements of this array may fit in an ordinary array.
     * </p>
     *
     * @return Array
     */
    public double[] toArray()
    {
        return this.toArray(0, this.size);
    }

    /**
     * Copies indicated range to an ordinary array.
     *
     * @param from From index, inclusive
     * @param to   To index, exclusive
     *
     * @return Array
     */
    public double[] toArray(long from, long to)
    {
        // Upper bound of a single array seems to be Integer.MAX_VALUE - 5
        int length, segment, offset, i;
        double[] array;

        length = (int)(to - from);
        array = new double[length];

        // Determine initial indices for this array
        segment = getSegment(from);
        offset = getOffset(from);

        for (i = 0; i < length; i++)
        {
            if (offset >= MAX_SIZE_ARRAY)
            {
                offset = 0;
                segment++;
            }

            array[i] = values[segment][offset];

            offset++;
        }

        return array;
    }

    /**
     * Clones the array.
     *
     * @return Cloned array
     */
    public LargeDoubleArray clone()
    {
        LargeDoubleArray clonedArray;

        try
        {
            clonedArray = (LargeDoubleArray)super.clone();
            clonedArray.values = values.clone();
            return clonedArray;
        }
        catch (CloneNotSupportedException e)
        {
            return null;
        }
    }

    /**
     * Iterable starting from a certain element
     */
    public class FromIterable implements Iterable<Double>
    {
        long from;

        public FromIterable(long from)
        {
            this.from = from;
        }

        @Override
        public java.util.Iterator<Double> iterator()
        {
            return new Iterator(from);
        }
    }

    /**
     * Iterable starting from a certain element to a certain element
     */
    public class FromToIterable implements Iterable<Double>
    {
        long from;
        long to;

        public FromToIterable(long from, long to)
        {
            this.from = from;
            this.to = to;
        }

        @Override
        public java.util.Iterator<Double> iterator()
        {
            return new Iterator(from, to);
        }
    }

    /**
     * Iterator capable of iterating over specified range.
     */
    public class Iterator implements PrimitiveIterator.OfDouble
    {
        private final int maxSegment;
        private final int maxOffset;

        private int currentMaxOffset;

        private int segment;
        private int offset;

        private boolean hasNext;

        /**
         * Constructs an iterator that iterates over all elements.
         */
        public Iterator()
        {
            maxSegment = values.length - 1;
            maxOffset = values[maxSegment].length;

            segment = 0;
            offset = 0;

            currentMaxOffset = values[segment].length;

            hasNext = (offset < currentMaxOffset);
        }

        /**
         * Constructs an iterator that iterates over all elements, starting from
         * some element.
         *
         * @param from From index, inclusive
         */
        public Iterator(long from)
        {
            maxSegment = values.length - 1;
            maxOffset = values[maxSegment].length;

            // Determine initial indices starting from
            segment = getSegment(from);
            offset = getOffset(from);

            currentMaxOffset = values[segment].length;

            hasNext = (offset < currentMaxOffset);
        }

        /**
         * Constructs an iterator that iterates over some range, starting from
         * some element to some element.
         *
         * @param from From index, inclusive
         * @param to   To index, exclusive
         */
        public Iterator(long from, long to)
        {
            maxSegment = getSegment(to);
            maxOffset = getOffset(to);

            // Determine initial indices starting from long
            segment = getSegment(from);
            offset = getOffset(from);

            currentMaxOffset = values[segment].length;

            if (segment == maxSegment)
                currentMaxOffset = maxOffset;
            else
                currentMaxOffset = LargeDoubleArray.MAX_SIZE_ARRAY;

            hasNext = (offset < currentMaxOffset);
        }

        /**
         * Provides next value in array
         *
         * @return Next value
         */
        @Override
        public double nextDouble()
        {
            double val;
            val = values[segment][offset];
            offset++;
            if (offset >= currentMaxOffset)
            {
                offset = 0;
                segment++;
                if (segment > maxSegment)
                    hasNext = false;
                else if (segment == maxSegment)
                    currentMaxOffset = maxOffset;
                else
                    currentMaxOffset = LargeDoubleArray.MAX_SIZE_ARRAY;

                if (offset >= currentMaxOffset)
                    hasNext = false;                
            }
            return val;
        }

        /**
         * Indicates whether there are any elements left.
         *
         * @return Boolean, true if there are elements left, false otherwise
         */
        @Override
        public boolean hasNext()
        {
            return hasNext;
        }
    }

}

