package nl.cwts.networkanalysis;

import nl.cwts.util.FastMath;

/**
 * Abstract base class for layout algorithms that use the VOS quality function.
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 */
public abstract class VOSLayoutAlgorithm implements Cloneable, QualityLayoutAlgorithm
{
    /**
     * Default attraction parameter.
     */
    public static final int DEFAULT_ATTRACTION = 2;

    /**
     * Default repulsion parameter.
     */
    public static final int DEFAULT_REPULSION = 1;

    /**
     * Default edge weight increment parameter.
     */
    public static final double DEFAULT_EDGE_WEIGHT_INCREMENT = 0;

    /**
     * Attraction parameter.
     */
    protected int attraction;

    /**
     * Repulsion parameter.
     */
    protected int repulsion;

    /**
     * Edge weight increment parameter.
     */
    protected double edgeWeightIncrement;

    /**
     * Constructs a VOS layout algorithm.
     */
    public VOSLayoutAlgorithm()
    {
        this(DEFAULT_ATTRACTION, DEFAULT_REPULSION, DEFAULT_EDGE_WEIGHT_INCREMENT);
    }

    /**
     * Constructs a VOS layout algorithm with a specified attraction parameter,
     * repulsion parameter, and edge weight increment parameter.
     *
     * @param attraction          Attraction parameter
     * @param repulsion           Repulsion parameter
     * @param edgeWeightIncrement Edge weight increment parameter
     */
    public VOSLayoutAlgorithm(int attraction, int repulsion, double edgeWeightIncrement)
    {
        this.attraction = attraction;
        this.repulsion = repulsion;
        this.edgeWeightIncrement = edgeWeightIncrement;
    }

    /**
     * Clones the algorithm.
     *
     * @return Cloned algorithm
     */
    public VOSLayoutAlgorithm clone()
    {
        try
        {
            return (VOSLayoutAlgorithm)super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            return null;
        }
    }

    /**
     * Returns the attraction parameter.
     *
     * @return Attraction parameter
     */
    public int getAttraction()
    {
        return attraction;
    }

    /**
     * Returns the repulsion parameter.
     *
     * @return Repulsion parameter
     */
    public int getRepulsion()
    {
        return repulsion;
    }

    /**
     * Returns the edge weight increment parameter.
     *
     * @return Edge weight increment parameter
     */
    public double getEdgeWeightIncrement()
    {
        return edgeWeightIncrement;
    }

    /**
     * Sets the attraction parameter.
     *
     * @param attraction Attraction parameter
     */
    public void setAttraction(int attraction)
    {
        this.attraction = attraction;
    }

    /**
     * Sets the repulsion parameter.
     *
     * @param repulsion Repulsion parameter
     */
    public void setRepulsion(int repulsion)
    {
        this.repulsion = repulsion;
    }

    /**
     * Sets the edge weight increment parameter.
     *
     * @param edgeWeightIncrement Edge weight increment parameter
     */
    public void setEdgeWeightIncrement(double edgeWeightIncrement)
    {
        this.edgeWeightIncrement = edgeWeightIncrement;
    }

    /**
     * Calculates the quality of a layout using the VOS quality function.
     *
     * <p>
     * The VOS quality function is given by
     * </p>
     *
     * <blockquote> {@code 1 / attraction * sum(a[i][j] * d(x[i], x[j]) ^
     * attraction) - 1 / repulsion * sum(d(x[i], x[j]) ^
     * repulsion)}, </blockquote>
     *
     * <p>
     * where {@code a[i][j]} is the weight of the edge between nodes {@code i}
     * and {@code j} and {@code x[i] = (x[i][1], x[i][2])} are the coordinates
     * of node {@code i}. The function {@code d(x[i], x[j])} is the Euclidean
     * distance between nodes {@code i} and {@code j}. The sum is taken over all
     * pairs of nodes {@code i} and {@code j} with {@code j < i}. The attraction
     * parameter must be greater than the repulsion parameter. The lower the
     * value of the VOS quality function, the higher the quality of the layout.
     * </p>
     *
     * @param network Network
     * @param layout  Layout
     *
     * @return Quality of the layout
     */
    public double calcQuality(Network network, Layout layout)
    {
        double distance, distance1, distance2, quality;
        int i, j;
        long k;

        quality = 0;

        for (i = 0; i < network.nNodes; i++)
            for (k = network.firstNeighborIndices[i]; k < network.firstNeighborIndices[i + 1]; k++)
                if (network.neighbors.get(k) < i)
                {
                    distance1 = layout.coordinates[0][i] - layout.coordinates[0][network.neighbors.get(k)];
                    distance2 = layout.coordinates[1][i] - layout.coordinates[1][network.neighbors.get(k)];
                    distance = Math.sqrt(distance1 * distance1 + distance2 * distance2);
                    if (attraction != 0)
                        quality += network.edgeWeights.get(k) * FastMath.fastPow(distance, attraction) / attraction;
                    else
                        quality += network.edgeWeights.get(k) * Math.log(distance);
                }

        for (i = 0; i < network.nNodes; i++)
            for (j = 0; j < i; j++)
            {
                distance1 = layout.coordinates[0][i] - layout.coordinates[0][j];
                distance2 = layout.coordinates[1][i] - layout.coordinates[1][j];
                distance = Math.sqrt(distance1 * distance1 + distance2 * distance2);

                if (repulsion != 0)
                    quality -= network.nodeWeights[i] * network.nodeWeights[j]
                            * FastMath.fastPow(distance, repulsion) / repulsion;
                else
                    quality -= network.nodeWeights[i] * network.nodeWeights[j] * Math.log(distance);

                if (edgeWeightIncrement > 0)
                    if (attraction != 0)
                        quality += edgeWeightIncrement * FastMath.fastPow(distance, attraction) / attraction;
                    else
                        quality += edgeWeightIncrement * Math.log(distance);
            }

        return quality;
    }
}
