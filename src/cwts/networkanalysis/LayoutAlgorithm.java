package cwts.networkanalysis;

/**
 * Interface for layout algorithms.
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 */
public interface LayoutAlgorithm
{
    /**
     * Finds a layout of the nodes in a network.
     *
     * @param network Network
     *
     * @return Layout
     */
    public Layout findLayout(Network network);
}
