package cwts.networkanalysis;

import cwts.util.FastMath;

/**
 * VOSLayoutAlgorithm
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 */
public abstract class VOSLayoutAlgorithm implements Cloneable, QualityLayoutAlgorithm
{
    public static final int DEFAULT_ATTRACTION = 2;
    public static final int DEFAULT_REPULSION = 1;
    public static final double DEFAULT_EDGE_WEIGHT_INCREMENT = 0;

    protected int attraction;
    protected int repulsion;
    protected double edgeWeightIncrement;

    public VOSLayoutAlgorithm()
    {
        this(DEFAULT_ATTRACTION, DEFAULT_REPULSION, DEFAULT_EDGE_WEIGHT_INCREMENT);
    }

    public VOSLayoutAlgorithm(int attraction, int repulsion, double edgeWeightIncrement)
    {
        this.attraction = attraction;
        this.repulsion = repulsion;
        this.edgeWeightIncrement = edgeWeightIncrement;
    }

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

    public double calcQuality(Network network, Layout layout)
    {
        double distance, distance1, distance2, qualityFunction;
        int i, j;

        qualityFunction = 0;

        for (i = 0; i < network.nNodes; i++)
            for (j = network.firstNeighborIndices[i]; j < network.firstNeighborIndices[i + 1]; j++)
                if (network.neighbors[j] < i)
                {
                    distance1 = layout.coordinates[0][i] - layout.coordinates[0][network.neighbors[j]];
                    distance2 = layout.coordinates[1][i] - layout.coordinates[1][network.neighbors[j]];
                    distance = Math.sqrt(distance1 * distance1 + distance2 * distance2);
                    if (attraction != 0)
                        qualityFunction += network.edgeWeights[j] * FastMath.fastPow(distance, attraction) / attraction;
                    else
                        qualityFunction += network.edgeWeights[j] * Math.log(distance);
                }

        for (i = 0; i < network.nNodes; i++)
            for (j = 0; j < i; j++)
            {
                distance1 = layout.coordinates[0][i] - layout.coordinates[0][j];
                distance2 = layout.coordinates[1][i] - layout.coordinates[1][j];
                distance = Math.sqrt(distance1 * distance1 + distance2 * distance2);
                if (repulsion != 0)
                    qualityFunction -= network.nodeWeights[i] * network.nodeWeights[j]
                            * FastMath.fastPow(distance, repulsion) / repulsion;
                else
                    qualityFunction -= network.nodeWeights[i] * network.nodeWeights[j] * Math.log(distance);
            }

        if (edgeWeightIncrement > 0)
            for (i = 0; i < network.nNodes; i++)
                for (j = 0; j < i; j++)
                {
                    distance1 = layout.coordinates[0][i] - layout.coordinates[0][j];
                    distance2 = layout.coordinates[1][i] - layout.coordinates[1][j];
                    distance = Math.sqrt(distance1 * distance1 + distance2 * distance2);
                    if (attraction != 0)
                        qualityFunction += edgeWeightIncrement * FastMath.fastPow(distance, attraction) / attraction;
                    else
                        qualityFunction += edgeWeightIncrement * Math.log(distance);
                }

        return qualityFunction;
    }
}
