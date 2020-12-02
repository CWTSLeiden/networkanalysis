package nl.cwts.util;

import java.util.Random;

/**
 * Utility functions for arrays.
 *
 * <p>
 * All methods in this class are static.
 * </p>
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 * @author Vincent Traag
 */
public final class Arrays
{
    /**
     * Calculates the sum of the values in an array.
     *
     * @param values Values
     *
     * @return Sum of values
     */
    public static double calcSum(double[] values)
    {
        double sum;
        int i;

        sum = 0;
        for (i = 0; i < values.length; i++)
            sum += values[i];
        return sum;
    }

    /**
     * Calculates the sum of the values in an array, considering only array
     * elements within a specified range.
     *
     * <p>
     * The sum is calculated over the elements
     * {@code values[beginIndex], ..., values[endIndex - 1]}.
     * </p>
     *
     * @param values     Values
     * @param beginIndex Begin index
     * @param endIndex   End index
     *
     * @return Sum of values
     */
    public static double calcSum(double[] values, int beginIndex, int endIndex)
    {
        double sum;
        int i;

        sum = 0;
        for (i = beginIndex; i < endIndex; i++)
            sum += values[i];
        return sum;
    }

    /**
     * Calculates the average of the values in an array.
     *
     * @param values Values
     *
     * @return Average value
     */
    public static double calcAverage(double[] values)
    {
        return calcSum(values) / values.length;
    }

    /**
     * Calculates the median of the values in an array.
     *
     * @param values Values
     *
     * @return Median value
     */
    public static double calcMedian(double[] values)
    {
        double median;
        double[] sortedValues;

        sortedValues = values.clone();
        java.util.Arrays.sort(sortedValues);
        if (sortedValues.length % 2 == 1)
            median = sortedValues[(sortedValues.length - 1) / 2];
        else
            median = (sortedValues[sortedValues.length / 2 - 1] + sortedValues[sortedValues.length / 2]) / 2;
        return median;
    }

    /**
     * Calculates the minimum of the values in an array.
     *
     * @param values Values
     *
     * @return Minimum value
     */
    public static double calcMinimum(double[] values)
    {
        double minimum;
        int i;

        minimum = values[0];
        for (i = 1; i < values.length; i++)
            minimum = Math.min(minimum, values[i]);
        return minimum;
    }

    /**
     * Calculates the maximum of the values in an array.
     *
     * @param values Values
     *
     * @return Maximum value
     */
    public static double calcMaximum(double[] values)
    {
        double maximum;
        int i;

        maximum = values[0];
        for (i = 1; i < values.length; i++)
            maximum = Math.max(maximum, values[i]);
        return maximum;
    }

    /**
     * Calculates the minimum of the values in an array.
     *
     * @param values Values
     *
     * @return Minimum value
     */
    public static int calcMinimum(int[] values)
    {
        int i, minimum;

        minimum = values[0];
        for (i = 1; i < values.length; i++)
            minimum = Math.max(minimum, values[i]);
        return minimum;
    }

    /**
     * Calculates the maximum of the values in an array.
     *
     * @param values Values
     *
     * @return Maximum value
     */
    public static int calcMaximum(int[] values)
    {
        int i, maximum;

        maximum = values[0];
        for (i = 1; i < values.length; i++)
            maximum = Math.max(maximum, values[i]);
        return maximum;
    }

    /**
     * Creates a double array of ones.
     *
     * @param nElements Number of elements
     *
     * @return Array of ones
     */
    public static double[] createDoubleArrayOfOnes(int nElements)
    {
        return(repeat(1.0, nElements));
    }

    /**
     * Creates a double array of repeated elements.
     *
     * @param value Element to repeat
     * @param nElements Number of elements
     *
     * @return Array of repeated elements
     */
    public static int[] repeat(int value, int nElements)
    {
        int[] elements;
        elements = new int[nElements];
        java.util.Arrays.fill(elements, value);
        return elements;
    }

    /**
     * Creates an integer array of repeated elements.
     *
     * @param value Element to repeat
     * @param nElements Number of elements
     *
     * @return Array of repeated elements
     */
    public static double[] repeat(double value, int nElements)
    {
        double[] elements;
        elements = new double[nElements];
        java.util.Arrays.fill(elements, value);
        return elements;
    }

    /**
     * Creates a double array of random numbers.
     *
     * @param nElements Number of elements
     *
     * @return Array of random numbers
     */
    public static double[] createDoubleArrayOfRandomNumbers(int nElements)
    {
        return createDoubleArrayOfRandomNumbers(nElements, new Random());
    }

    /**
     * Creates a double array of random numbers.
     *
     * @param nElements Number of elements
     * @param random    Random number generator
     *
     * @return Array of random numbers
     */
    public static double[] createDoubleArrayOfRandomNumbers(int nElements, Random random)
    {
        double[] values;
        int i;

        values = new double[nElements];
        for (i = 0; i < nElements; i++)
            values[i] = random.nextDouble();
        return values;
    }

    /**
     * Generates a random permutation.
     *
     * <p>
     * A random permutation is generated of the integers
     * {@code 0, ..., nElements - 1}.
     * </p>
     *
     * @param nElements Number of elements
     *
     * @return Random permutation
     */
    public static int[] generateRandomPermutation(int nElements)
    {
        return generateRandomPermutation(nElements, new Random());
    }

    /**
     * Generates a random permutation.
     *
     * <p>
     * A random permutation is generated of the integers
     * {@code 0, ..., nElements - 1}.
     * </p>
     *
     * @param nElements Number of elements
     * @param random    Random number generator
     *
     * @return Random permutation
     */
    public static int[] generateRandomPermutation(int nElements, Random random)
    {
        int i;
        int[] permutation;

        permutation = new int[nElements];
        for (i = 0; i < nElements; i++)
            permutation[i] = i;
        permuteRandomly(permutation, random);
        return permutation;
    }

    /**
     * Randomly permutes the elements.
     *
     * <p>
     * Randomly permutes the elements 0, ..., nElements - 1.
     * </p>
     *
     * @param elements Elements
     * @param random   Random number generator
     */
    public static void permuteRandomly(int[] elements, Random random)
    {
        int i, j, k;

        for (i = 0; i < elements.length; i++)
        {
            j = random.nextInt(elements.length);
            k = elements[i];
            elements[i] = elements[j];
            elements[j] = k;
        }
    }

    private Arrays()
    {
    }
}
