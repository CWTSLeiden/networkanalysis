package nl.cwts.networkanalysis;

/**
 * Interface for layout algorithms that use a quality function.
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 */
public interface QualityLayoutAlgorithm extends LayoutAlgorithm
{
    /**
     * Calculates the quality of a layout of the nodes in a network.
     *
     * @param network Network
     * @param layout  Layout
     *
     * @return Quality of the layout
     */
    public double calcQuality(Network network, Layout layout);
}
