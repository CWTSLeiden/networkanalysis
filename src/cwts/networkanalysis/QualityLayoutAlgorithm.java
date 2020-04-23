package cwts.networkanalysis;

/**
 * QualityLayoutAlgorithm
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 */
public interface QualityLayoutAlgorithm extends LayoutAlgorithm
{
    public double calcQuality(Network network, Layout layout);
}
