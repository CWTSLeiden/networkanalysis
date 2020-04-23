package cwts.networkanalysis;

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
        return 0;
    }
}
