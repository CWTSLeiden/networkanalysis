package nl.cwts.networkanalysis;

import nl.cwts.util.LargeBooleanArray;
import nl.cwts.util.LargeDoubleArray;
import nl.cwts.util.LargeIntArray;
import nl.cwts.util.LargeLongArray;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.PrimitiveIterator;
import java.util.Random;

/**
 * Network.
 *
 * <p>
 * Weighted nodes and weighted edges are supported. Directed edges are not
 * supported.
 * </p>
 *
 * <p>
 * Network objects are immutable.
 * </p>
 *
 * <p>
 * The adjacency matrix of the network is stored in a sparse compressed format.
 * </p>
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 * @author Vincent Traag
 */
public class Network implements Serializable
{
    private static final long serialVersionUID = 1;

    public static final long MAX_N_EDGES = LargeDoubleArray.MAX_SIZE / 2;

    /**
     * Number of nodes.
     */
    protected int nNodes;

    /**
     * Number of edges.
     *
     * <p>
     * Each edge is counted twice, once in each direction.
     * </p>
     */
    protected long nEdges;

    /**
     * Node weights.
     */
    protected double[] nodeWeights;

    /**
     * Index of the first neighbor of each node in the (@code neighbors} array.
     *
     * <p>
     * The neighbors of node {@code i} are given by {@code
     * neighbors[firstNeighborIndices[i]], ...,
     * neighbors[firstNeighborIndices[i + 1] - 1]}.
     * </p>
     */
    protected long[] firstNeighborIndices;

    /**
     * Neighbors of each node.
     */
    protected LargeIntArray neighbors;

    /**
     * Edge weights.
     */
    protected LargeDoubleArray edgeWeights;

    /**
     * Total edge weight of self links.
     */
    protected double totalEdgeWeightSelfLinks;

    /**
     * Loads a network from a file.
     *
     * @param filename File from which a network is loaded
     *
     * @return Loaded network
     *
     * @throws ClassNotFoundException Class not found
     * @throws IOException            Could not read the file
     *
     * @see #save(String filename)
     */
    public static Network load(String filename) throws ClassNotFoundException, IOException
    {
        Network network;
        ObjectInputStream objectInputStream;

        objectInputStream = new ObjectInputStream(new FileInputStream(filename));

        network = (Network)objectInputStream.readObject();

        objectInputStream.close();

        return network;
    }

    /**
     * Constructs a network based on a list of edges.
     *
     * <p>
     * The list of edges is provided in the two-dimensional array {@code
     * edges}. Edge {@code i} connects nodes {@code edges[0].get(i)} and {@code
     * edges[1].get(i)}. Edges do not have weights. If {@code sortedEdges} is
     * false, the list of edges does not need to be sorted and each edge must
     * be included only once. If {@code sortedEdges}is true, the list of edges
     * must be sorted and each edge must be included twice, once in each
     * direction.
     * </p>
     *
     * @param nodeWeights    Node weights
     * @param edges          Edge list
     * @param sortedEdges    Indicates whether the edge list is sorted
     * @param checkIntegrity Indicates whether to check the integrity of the
     *                       network
     */
    public Network(double[] nodeWeights, LargeIntArray[] edges, boolean sortedEdges, boolean checkIntegrity)
    {
        this(nodeWeights.length, nodeWeights, false, edges, null, sortedEdges, checkIntegrity);
    }

    /**
     * Constructs a network based on a list of edges.
     *
     * <p>
     * The list of edges is provided in the two-dimensional array {@code
     * edges}. Edge {@code i} connects nodes {@code edges[0].get(i)} and {@code
     * edges[1].get(i)} and has weight {@code edgeWeights.get(i)}. If {@code
     * sortedEdges} is false, the list of edges does not need to be sorted and
     * each edge must be included only once. If {@code sortedEdges} is true,
     * the list of edges must be sorted and each edge must be included twice,
     * once in each direction.
     * </p>
     *
     * @param nodeWeights    Node weights
     * @param edges          Edge list
     * @param edgeWeights    Edge weights
     * @param sortedEdges    Indicates whether the edge list is sorted
     * @param checkIntegrity Indicates whether to check the integrity of the
     *                       network
     */
    public Network(double[] nodeWeights, LargeIntArray[] edges, LargeDoubleArray edgeWeights, boolean sortedEdges, boolean checkIntegrity)
    {
        this(nodeWeights.length, nodeWeights, false, edges, edgeWeights, sortedEdges, checkIntegrity);
    }

    /**
     * Constructs a network based on a list of neighbors.
     *
     * <p>
     * The list of neighbors is provided in the array {@code neighbors}. The
     * neighbors of node {@code i} are given by {@code
     * neighbors[firstNeighborIndices[i]], ...,
     * neighbors[firstNeighborIndices[i + 1] - 1]}. The array {@code
     * firstNeighborIndices} must have a length of the number of nodes plus 1.
     * The neighbors of a node must be listed in increasing order in the array
     * {@code neighbors}. Edges do not have weights.
     * </p>
     *
     * @param nodeWeights          Node weights
     * @param firstNeighborIndices Index of the first neighbor of each node
     * @param neighbors            Neighbor list
     * @param checkIntegrity       Indicates whether to check the integrity of
     *                             the network
     */
    public Network(double[] nodeWeights, long[] firstNeighborIndices, LargeIntArray neighbors, boolean checkIntegrity)
    {
        this(nodeWeights.length, nodeWeights, false, firstNeighborIndices, neighbors, null, checkIntegrity);
    }

    /**
     * Constructs a network based on a list of neighbors.
     *
     * <p>
     * The list of neighbors is provided in the array {@code neighbors}. The
     * neighbors of node {@code i} are given by {@code
     * neighbors[firstNeighborIndices[i]], ...,
     * neighbors[firstNeighborIndices[i + 1] - 1]}. The array {@code
     * firstNeighborIndices} must have a length of the number of nodes plus 1.
     * The neighbors of a node must be listed in increasing order in the array
     * {@code neighbors}. For each neighbor in the array {@code neighbors}, the
     * corresponding edge weight is provided in the array {@code edgeWeights}.
     * </p>
     *
     * @param nodeWeights          Node weights
     * @param firstNeighborIndices Index of the first neighbor of each node
     * @param neighbors            Neighbor list
     * @param edgeWeights          Edge weights
     * @param checkIntegrity       Indicates whether to check the integrity of
     *                             the network
     */
    public Network(double[] nodeWeights, long[] firstNeighborIndices, LargeIntArray neighbors, LargeDoubleArray edgeWeights, boolean checkIntegrity)
    {
        this(nodeWeights.length, nodeWeights, false, firstNeighborIndices, neighbors, edgeWeights, checkIntegrity);
    }

    /**
     * Constructs a network based on a list of edges.
     *
     * <p>
     * The list of edges is provided in the two-dimensional array {@code
     * edges}. Edge {@code i} connects nodes {@code edges[0].get(i)} and {@code
     * edges[1].get(i)}. Edges do not have weights. If {@code sortedEdges} is
     * false, the list of edges does not need to be sorted and each edge must
     * be included only once. If {@code sortedEdges}is true, the list of edges
     * must be sorted and each edge must be included twice, once in each
     * direction.
     * </p>
     *
     * <p>
     * If {@code setNodeWeightsToTotalEdgeWeights} is false, the weights of the
     * nodes are set to 1. If {@code setNodeWeightsToTotalEdgeWeights} is true,
     * the weight of a node is set equal to the total weight of the edges
     * between the node and its neighbors.
     * </p>
     *
     * @param nNodes                           Number of nodes
     * @param setNodeWeightsToTotalEdgeWeights Indicates whether to set node
     *                                         weights equal to total edge
     *                                         weights
     * @param edges                            Edge list
     * @param sortedEdges                      Indicates whether the edge list
     *                                         is sorted
     * @param checkIntegrity                   Indicates whether to check the
     *                                         integrity of the network
     */
    public Network(int nNodes, boolean setNodeWeightsToTotalEdgeWeights, LargeIntArray[] edges, boolean sortedEdges, boolean checkIntegrity)
    {
        this(nNodes, null, setNodeWeightsToTotalEdgeWeights, edges, null, sortedEdges, checkIntegrity);
    }

    /**
     * Constructs a network based on a list of edges.
     *
     * <p>
     * The list of edges is provided in the two-dimensional array {@code
     * edges}. Edge {@code i} connects nodes {@code edges[0].get(i)} and {@code
     * edges[1].get(i)} and has weight {@code edgeWeights.get(i)}. If {@code
     * sortedEdges} is false, the list of edges does not need to be sorted and
     * each edge must be included only once. If {@code sortedEdges} is true,
     * the list of edges must be sorted and each edge must be included twice,
     * once in each direction.
     * </p>
     *
     * <p>
     * If {@code setNodeWeightsToTotalEdgeWeights} is false, the weights of the
     * nodes are set to 1. If {@code setNodeWeightsToTotalEdgeWeights} is true,
     * the weight of a node is set equal to the total weight of the edges
     * between the node and its neighbors.
     * </p>
     *
     * @param nNodes                           Number of nodes
     * @param setNodeWeightsToTotalEdgeWeights Indicates whether to set node
     *                                         weights equal to total edge
     *                                         weights
     * @param edges                            Edge list
     * @param edgeWeights                      Edge weights
     * @param sortedEdges                      Indicates whether the edge list
     *                                         is sorted
     * @param checkIntegrity                   Indicates whether to check the
     *                                         integrity of the network
     */
    public Network(int nNodes, boolean setNodeWeightsToTotalEdgeWeights, LargeIntArray[] edges, LargeDoubleArray edgeWeights, boolean sortedEdges, boolean checkIntegrity)
    {
        this(nNodes, null, setNodeWeightsToTotalEdgeWeights, edges, edgeWeights, sortedEdges, checkIntegrity);
    }

    /**
     * Constructs a network based on a list of neighbors.
     *
     * <p>
     * The list of neighbors is provided in the array {@code neighbors}. The
     * neighbors of node {@code i} are given by {@code
     * neighbors[firstNeighborIndices[i]], ...,
     * neighbors[firstNeighborIndices[i + 1] - 1]}. The array {@code
     * firstNeighborIndices} must have a length of the number of nodes plus 1.
     * The neighbors of a node must be listed in increasing order in the array
     * {@code neighbors}. Edges do not have weights.
     * </p>
     *
     * <p>
     * If {@code setNodeWeightsToTotalEdgeWeights} is false, the weights of the
     * nodes are set to 1. If {@code setNodeWeightsToTotalEdgeWeights} is true,
     * the weight of a node is set equal to the total weight of the edges
     * between the node and its neighbors.
     * </p>
     *
     * @param nNodes                           Number of nodes
     * @param setNodeWeightsToTotalEdgeWeights Indicates whether to set node
     *                                         weights equal to total edge
     *                                         weights
     * @param firstNeighborIndices             Index of the first neighbor of
     *                                         each node
     * @param neighbors                        Neighbor list
     * @param checkIntegrity                   Indicates whether to check the
     *                                         integrity of the network
     */
    public Network(int nNodes, boolean setNodeWeightsToTotalEdgeWeights, long[] firstNeighborIndices, LargeIntArray neighbors, boolean checkIntegrity)
    {
        this(nNodes, null, setNodeWeightsToTotalEdgeWeights, firstNeighborIndices, neighbors, null, checkIntegrity);
    }

    /**
     * Constructs a network based on a list of neighbors.
     *
     * <p>
     * The list of neighbors is provided in the array {@code neighbors}. The
     * neighbors of node {@code i} are given by {@code
     * neighbors[firstNeighborIndices[i]], ...,
     * neighbors[firstNeighborIndices[i + 1] - 1]}. The array {@code
     * firstNeighborIndices} must have a length of the number of nodes plus 1.
     * The neighbors of a node must be listed in increasing order in the array
     * {@code neighbors}. For each neighbor in the array {@code neighbors}, the
     * corresponding edge weight is provided in the array {@code edgeWeights}.
     * </p>
     *
     * <p>
     * If {@code setNodeWeightsToTotalEdgeWeights} is false, the weights of the
     * nodes are set to 1. If {@code setNodeWeightsToTotalEdgeWeights} is true,
     * the weight of a node is set equal to the total weight of the edges
     * between the node and its neighbors.
     * </p>
     *
     * @param nNodes                           Number of nodes
     * @param setNodeWeightsToTotalEdgeWeights Indicates whether to set node
     *                                         weights equal to total edge
     *                                         weights
     * @param firstNeighborIndices             Index of the first neighbor of
     *                                         each node
     * @param neighbors                        Neighbor list
     * @param edgeWeights                      Edge weights
     * @param checkIntegrity                   Indicates whether to check the
     *                                         integrity of the network
     */
    public Network(int nNodes, boolean setNodeWeightsToTotalEdgeWeights, long[] firstNeighborIndices, LargeIntArray neighbors, LargeDoubleArray edgeWeights, boolean checkIntegrity)
    {
        this(nNodes, null, setNodeWeightsToTotalEdgeWeights, firstNeighborIndices, neighbors, edgeWeights, checkIntegrity);
    }

    /**
     * Saves the network in a file.
     *
     * @param filename File in which the network is saved
     *
     * @throws IOException Could not write to the file
     *
     * @see #load(String filename)
     */
    public void save(String filename) throws IOException
    {
        ObjectOutputStream objectOutputStream;

        objectOutputStream = new ObjectOutputStream(new FileOutputStream(filename));

        objectOutputStream.writeObject(this);

        objectOutputStream.close();
    }

    /**
     * Returns the number of nodes.
     *
     * @return Number of nodes
     */
    public int getNNodes()
    {
        return nNodes;
    }

    /**
     * Returns the total node weight.
     *
     * @return Total node weight
     */
    public double getTotalNodeWeight()
    {
        return nl.cwts.util.Arrays.calcSum(nodeWeights);
    }

    /**
     * Returns the weight of each node.
     *
     * @return Weight of each node
     */
    public double[] getNodeWeights()
    {
        return nodeWeights.clone();
    }

    /**
     * Returns the weight of a node.
     *
     * @param node Node
     *
     * @return Weight
     */
    public double getNodeWeight(int node)
    {
        return nodeWeights[node];
    }

    /**
     * Returns the number of edges.
     *
     * <p>
     * Each edge is counted only once, even though an edge runs in two
     * directions. This means that the number of edges returned by {@link
     * #getEdges()} equals twice the number of edges returned by {@link
     * #getNEdges()}.
     * </p>
     *
     * @return Number of edges
     */
    public long getNEdges()
    {
        return nEdges / 2;
    }

    /**
     * Returns the number of neighbors per node.
     *
     * @return Number of neighbors per node
     */
    public int[] getNNeighborsPerNode()
    {
        int i;
        int[] nNeighborsPerNode;

        nNeighborsPerNode = new int[nNodes];
        for (i = 0; i < nNodes; i++)
            nNeighborsPerNode[i] = (int)(firstNeighborIndices[i + 1] - firstNeighborIndices[i]);
        return nNeighborsPerNode;
    }

    /**
     * Returns the number of neighbors of a node.
     *
     * @param node Node
     *
     * @return Number of neighbors
     */
    public int getNNeighbors(int node)
    {
        return (int)(firstNeighborIndices[node + 1] - firstNeighborIndices[node]);
    }

    /**
     * Returns the list of edges.
     *
     * <p>
     * Each edge is included twice, once in each direction. This means that the
     * number of edges returned by {@link #getEdges()} equals twice the number
     * of edges returned by {@link #getNEdges()}.
     * </p>
     *
     * <p>
     * The list of edges is returned in a two-dimensional array {@code edges}.
     * Edge {@code i} connects nodes {@code edges[0].get(i)} and {@code
     * edges[1].get(i)}.
     * </p>
     *
     * @return List of edges
     */
    public LargeIntArray[] getEdges()
    {
        int i;
        LargeIntArray[] edges;
        edges = new LargeIntArray[2];
        edges[0] = new LargeIntArray(nEdges);
        for (i = 0; i < nNodes; i++)
            edges[0].fill(firstNeighborIndices[i], firstNeighborIndices[i + 1], i);
        edges[1] = neighbors.clone();
        return edges;
    }

    /**
     * Returns a list of neighbors per node.
     *
     * @return List of neighbors per node
     */
    public int[][] getNeighborsPerNode()
    {
        int i;
        int[][] neighborsPerNode;

        neighborsPerNode = new int[nNodes][];
        for (i = 0; i < nNodes; i++)
            neighborsPerNode[i] = neighbors.toArray(firstNeighborIndices[i], firstNeighborIndices[i + 1]);
        return neighborsPerNode;
    }

    /**
     * Returns the list of neighbors of a node.
     *
     * @param node Node
     *
     * @return List of neighbors
     */
    public int[] getNeighbors(int node)
    {
        return neighbors.toArray(firstNeighborIndices[node], firstNeighborIndices[node + 1]);
    }

    /**
     * Returns an iterable over all the neighbors of a node.
     *
     * @param node Node
     * @return Iterable over neighbors
     */
    public LargeIntArray.FromToIterable neighbors(int node)
    {
        return neighbors.fromTo(firstNeighborIndices[node], firstNeighborIndices[node + 1]);
    }

    /**
     * Returns an iterable over all the incident edges of a node.
     *
     * @param node Node
     * @return Iterable over incident edges.
     */
    public RangeIterable incidentEdges(int node)
    {
        return new RangeIterable(firstNeighborIndices[node], firstNeighborIndices[node + 1]);
    }

    /**
     * Returns the total edge weight.
     *
     * <p>
     * Each edge is considered only once, even though an edge runs in two
     * directions. This means that the sum of the edge weights returned by
     * {@link #getEdgeWeights()} equals twice the total edge weight returned by
     * {@link #getTotalEdgeWeight()}.
     * </p>
     *
     * <p>
     * Edge weights of self links are not included.
     * </p>
     *
     * @return Total edge weight
     */
    public double getTotalEdgeWeight()
    {
        return edgeWeights.calcSum() / 2;
    }

    /**
     * Returns the total edge weight per node. The total edge weight of a node
     * equals the sum of the weights of the edges between the node and its
     * neighbors.
     *
     * @return Total edge weight per node
     */
    public double[] getTotalEdgeWeightPerNode()
    {
        return getTotalEdgeWeightPerNodeHelper();
    }

    /**
     * Returns the total edge weight of a node. The total edge weight of a node
     * equals the sum of the weights of the edges between the node and its
     * neighbors.
     *
     * @param node Node
     *
     * @return Total edge weight
     */
    public double getTotalEdgeWeight(int node)
    {
        return edgeWeights.calcSum(firstNeighborIndices[node], firstNeighborIndices[node + 1]);
    }

    /**
     * Returns the edge weights.
     *
     * <p>
     * Each edge is included twice, once in each direction. This means that the
     * sum of the edge weights returned by {@link #getEdgeWeights()} equals
     * twice the total edge weight returned by {@link #getTotalEdgeWeight()}.
     * </p>
     *
     * @return Edge weights
     */
    public LargeDoubleArray getEdgeWeights()
    {
        return edgeWeights.clone();
    }

    /**
     * Returns a list of edge weights per node. These are the weights of the
     * edges between a node and its neighbors.
     *
     * @return List of edge weights per node
     */
    public double[][] getEdgeWeightsPerNode()
    {
        double[][] edgeWeightsPerNode;
        int i;

        edgeWeightsPerNode = new double[nNodes][];
        for (i = 0; i < nNodes; i++)
            edgeWeightsPerNode[i] = edgeWeights.toArray(firstNeighborIndices[i], firstNeighborIndices[i + 1]);
        return edgeWeightsPerNode;
    }

    /**
     * Returns the list of edge weights of a node. These are the weights of the
     * edges between the node and its neighbors.
     *
     * @param node Node
     *
     * @return List of edge weights
     */
    public double[] getEdgeWeights(int node)
    {
        return edgeWeights.toArray(firstNeighborIndices[node], firstNeighborIndices[node + 1]);
    }

    /**
     * Returns an iterable over all the edge weights of all incident edges of a
     * node.
     *
     * @param node Node
     * @return Iterable over edge weights of a node.
     */
    public LargeDoubleArray.FromToIterable edgeWeights(int node)
    {
        return edgeWeights.fromTo(firstNeighborIndices[node], firstNeighborIndices[node + 1]);
    }

    /**
     * Returns the total edge weight of self links.
     *
     * @return Total edge weight of self links
     */
    public double getTotalEdgeWeightSelfLinks()
    {
        return totalEdgeWeightSelfLinks;
    }

    /**
     * Creates a copy of the network, but without node weights.
     *
     * <p>
     * Each node is assigned a weight of 1.
     * </p>
     *
     * @return Network without node weights
     */
    public Network createNetworkWithoutNodeWeights()
    {
        Network networkWithoutNodeWeights;

        networkWithoutNodeWeights = new Network();
        networkWithoutNodeWeights.nNodes = nNodes;
        networkWithoutNodeWeights.nEdges = nEdges;
        networkWithoutNodeWeights.nodeWeights = nl.cwts.util.Arrays.createDoubleArrayOfOnes(nNodes);
        networkWithoutNodeWeights.firstNeighborIndices = firstNeighborIndices;
        networkWithoutNodeWeights.neighbors = neighbors;
        networkWithoutNodeWeights.edgeWeights = edgeWeights;
        networkWithoutNodeWeights.totalEdgeWeightSelfLinks = totalEdgeWeightSelfLinks;
        return networkWithoutNodeWeights;
    }

    /**
     * Creates a copy of the network, but without edge weights.
     *
     * <p>
     * Each edge is assigned a weight of 1.
     * </p>
     *
     * @return Network without edge weights
     */
    public Network createNetworkWithoutEdgeWeights()
    {
        Network networkWithoutEdgeWeights;

        networkWithoutEdgeWeights = new Network();
        networkWithoutEdgeWeights.nNodes = nNodes;
        networkWithoutEdgeWeights.nEdges = nEdges;
        networkWithoutEdgeWeights.nodeWeights = nodeWeights;
        networkWithoutEdgeWeights.firstNeighborIndices = firstNeighborIndices;
        networkWithoutEdgeWeights.neighbors = neighbors;
        networkWithoutEdgeWeights.edgeWeights = new LargeDoubleArray(nEdges, 1);
        networkWithoutEdgeWeights.totalEdgeWeightSelfLinks = 0;
        return networkWithoutEdgeWeights;
    }

    /**
     * Creates a copy of the network, but without node and edge weights.
     *
     * <p>
     * Each node is assigned a weight of 1, and each edge is assigned a weight
     * of 1.
     * </p>
     *
     * @return Network without node and edge weights
     */
    public Network createNetworkWithoutNodeAndEdgeWeights()
    {
        Network networkWithoutNodeAndEdgeWeights;

        networkWithoutNodeAndEdgeWeights = new Network();
        networkWithoutNodeAndEdgeWeights.nNodes = nNodes;
        networkWithoutNodeAndEdgeWeights.nEdges = nEdges;
        networkWithoutNodeAndEdgeWeights.nodeWeights = nl.cwts.util.Arrays.createDoubleArrayOfOnes(nNodes);
        networkWithoutNodeAndEdgeWeights.firstNeighborIndices = firstNeighborIndices;
        networkWithoutNodeAndEdgeWeights.neighbors = neighbors;
        networkWithoutNodeAndEdgeWeights.edgeWeights = new LargeDoubleArray(nEdges, 1);
        networkWithoutNodeAndEdgeWeights.totalEdgeWeightSelfLinks = 0;
        return networkWithoutNodeAndEdgeWeights;
    }

    /**
     * Creates a copy of the network in which the edge weights have been
     * normalized using the association strength.
     *
     * <p>
     * The normalized weight {@code a'[i][j]} of the edge between nodes {@code
     * i} and {@code j} is given by
     * </p>
     *
     * <blockquote>
     * {@code a'[i][j] = a[i][j] / (n[i] * n[j] / (2 * m))},
     * </blockquote>
     *
     * <p>
     * where {@code a[i][j]} is the non-normalized weight of the edge between
     * nodes {@code i} and {@code j}, {@code n[i]} is the weight of node {@code
     * i}, and {@code m} is half the total node weight.
     * </p>
     *
     * <p>
     * If each node's weight equals the total weight of the edges between the
     * node and its neighbors, the edge weights are normalized by dividing them
     * by the expected edge weights in the random configuration model.
     * </p>
     *
     * <p>
     * The node weights are set to 1.
     * </p>
     *
     * @return Normalized network
     */
    public Network createNormalizedNetworkUsingAssociationStrength()
    {
        double totalNodeWeight;
        int i;
        long j;
        Network normalizedNetwork;

        normalizedNetwork = new Network();

        normalizedNetwork.nNodes = nNodes;
        normalizedNetwork.nEdges = nEdges;
        normalizedNetwork.nodeWeights = nl.cwts.util.Arrays.createDoubleArrayOfOnes(nNodes);
        normalizedNetwork.firstNeighborIndices = firstNeighborIndices;
        normalizedNetwork.neighbors = neighbors;

        normalizedNetwork.edgeWeights = new LargeDoubleArray(nEdges);
        totalNodeWeight = getTotalNodeWeight();
        for (i = 0; i < nNodes; i++)
            for (j = firstNeighborIndices[i]; j < firstNeighborIndices[i + 1]; j++)
                normalizedNetwork.edgeWeights.divide(j, ((nodeWeights[i] * nodeWeights[neighbors.get(j)]) / totalNodeWeight));

        normalizedNetwork.totalEdgeWeightSelfLinks = 0;

        return normalizedNetwork;
    }

    /**
     * Creates a copy of the network in which the edge weights have been
     * normalized using fractionalization.
     *
     * <p>
     * The normalized weight {@code a'[i][j]} of the edge between nodes {@code
     * i} and {@code j} is given by
     * </p>
     *
     * <blockquote>
     * {@code a'[i][j] = a[i][j] * (n / n[i] + n / n[j]) / 2},
     * </blockquote>
     *
     * <p>
     * where {@code a[i][j]} is the non-normalized weight of the edge between
     * nodes {@code i} and {@code j}, {@code n[i]} is the weight of node {@code
     * i}, and {@code n} is the number of nodes.
     * </p>
     *
     * <p>
     * The node weights are set to 1.
     * </p>
     *
     * @return Normalized network
     */
    public Network createNormalizedNetworkUsingFractionalization()
    {
        int i;
        long j;
        Network normalizedNetwork;

        normalizedNetwork = new Network();

        normalizedNetwork.nNodes = nNodes;
        normalizedNetwork.nEdges = nEdges;
        normalizedNetwork.nodeWeights = nl.cwts.util.Arrays.createDoubleArrayOfOnes(nNodes);
        normalizedNetwork.firstNeighborIndices = firstNeighborIndices;
        normalizedNetwork.neighbors = neighbors;

        normalizedNetwork.edgeWeights = new LargeDoubleArray(nEdges);
        for (i = 0; i < nNodes; i++)
            for (j = firstNeighborIndices[i]; j < firstNeighborIndices[i + 1]; j++)
                normalizedNetwork.edgeWeights.divide(j, (2 / (nNodes / nodeWeights[i] + nNodes / nodeWeights[neighbors.get(j)])));

        normalizedNetwork.totalEdgeWeightSelfLinks = 0;

        return normalizedNetwork;
    }

    /**
     * Creates a copy of the network that has been pruned in order to have a
     * specified maximum number of edges.
     *
     * <p>
     * Only the edges with the highest weights are retained in the pruned
     * network. In case of ties, the edges to be retained are selected
     * randomly.
     * </p>
     *
     * @param maxNEdges Maximum number of edges
     *
     * @return Pruned network
     */
    public Network createPrunedNetwork(int maxNEdges)
    {
        return createPrunedNetwork(maxNEdges, new Random());
    }

    /**
     * Creates a copy of the network that has been pruned in order to have a
     * specified maximum number of edges.
     *
     * <p>
     * Only the edges with the highest weights are retained in the pruned
     * network. In case of ties, the edges to be retained are selected
     * randomly.
     * </p>
     *
     * @param maxNEdges Maximum number of edges
     * @param random    Random number generator
     *
     * @return Pruned network
     */
    public Network createPrunedNetwork(long maxNEdges, Random random)
    {
        double edgeWeightThreshold, randomNumberThreshold;
        LargeDoubleArray edgeWeights;
        LargeDoubleArray randomNumbers, randomNumbersEdgesAtThreshold;
        int j;
        long i, nEdgesAboveThreshold, nEdgesAtThreshold;
        long k;
        Network prunedNetwork;

        maxNEdges *= 2;

        if (maxNEdges >= nEdges)
            return this;

        edgeWeights = new LargeDoubleArray(nEdges / 2);
        i = 0;
        for (j = 0; j < nNodes; j++)
        {
            k = firstNeighborIndices[j];
            while ((k < firstNeighborIndices[j + 1]) && (neighbors.get(k) < j))
            {
                edgeWeights.set(i, this.edgeWeights.get(k));
                i++;
                k++;
            }
        }
        edgeWeights.sort();
        edgeWeightThreshold = edgeWeights.get((nEdges - maxNEdges) / 2);

        nEdgesAboveThreshold = 0;
        while (edgeWeights.get(nEdges / 2 - nEdgesAboveThreshold - 1) > edgeWeightThreshold)
            nEdgesAboveThreshold++;
        nEdgesAtThreshold = 0;
        while ((nEdgesAboveThreshold + nEdgesAtThreshold < nEdges / 2) && (edgeWeights.get(nEdges / 2 - nEdgesAboveThreshold - nEdgesAtThreshold - 1) == edgeWeightThreshold))
            nEdgesAtThreshold++;

        randomNumbers = new LargeDoubleArray(nNodes * nNodes);
        for (i = 0; i < randomNumbers.size(); i++)
            randomNumbers.set(i, random.nextDouble());

        randomNumbersEdgesAtThreshold = new LargeDoubleArray(nEdgesAtThreshold);
        i = 0;
        for (j = 0; j < nNodes; j++)
        {
            k = firstNeighborIndices[j];
            while ((k < firstNeighborIndices[j + 1]) && (neighbors.get(k) < j))
            {
                if (this.edgeWeights.get(k) == edgeWeightThreshold)
                {
                    randomNumbersEdgesAtThreshold.set(i,
                                                      getRandomNumber(j,
                                                                      neighbors.get(k), randomNumbers));
                    i++;
                }
                k++;
            }
        }
        randomNumbersEdgesAtThreshold.sort();
        randomNumberThreshold =
                randomNumbersEdgesAtThreshold.get(nEdgesAboveThreshold + nEdgesAtThreshold - maxNEdges / 2);

        prunedNetwork = new Network();

        prunedNetwork.nNodes = nNodes;
        prunedNetwork.nEdges = maxNEdges;
        prunedNetwork.nodeWeights = nodeWeights;

        prunedNetwork.firstNeighborIndices = new long[nNodes + 1];
        prunedNetwork.neighbors = new LargeIntArray(maxNEdges);
        prunedNetwork.edgeWeights = new LargeDoubleArray(maxNEdges);
        i = 0;
        for (j = 0; j < nNodes; j++)
        {
            for (k = firstNeighborIndices[j]; k < firstNeighborIndices[j + 1]; k++)
                if ((this.edgeWeights.get(k) > edgeWeightThreshold) || ((this.edgeWeights.get(k) == edgeWeightThreshold) && (getRandomNumber(j, neighbors.get(k), randomNumbers) >= randomNumberThreshold)))
                {
                    prunedNetwork.neighbors.set(i, neighbors.get(k));
                    prunedNetwork.edgeWeights.set(i, this.edgeWeights.get(k));
                    i++;
                }
            prunedNetwork.firstNeighborIndices[j + 1] = i;
        }

        prunedNetwork.totalEdgeWeightSelfLinks = 0;

        return prunedNetwork;
    }

    /**
     * Creates an induced subnetwork for specified nodes.
     *
     * @param nodes Nodes
     *
     * @return Subnetwork
     */
    public Network createSubnetwork(int[] nodes)
    {
        LargeDoubleArray subnetworkEdgeWeights;
        int i, j;
        long k;
        int[] subnetworkNodes;
        LargeIntArray subnetworkNeighbors;
        Network subnetwork;

        subnetwork = new Network();

        subnetwork.nNodes = nodes.length;

        if (subnetwork.nNodes == 1)
        {
            subnetwork.nEdges = 0;
            subnetwork.nodeWeights = new double[1];
            subnetwork.nodeWeights[0] = nodeWeights[nodes[0]];
            subnetwork.firstNeighborIndices = new long[2];
            subnetwork.neighbors = new LargeIntArray(0);
            subnetwork.edgeWeights = new LargeDoubleArray(0);
        }
        else
        {
            subnetworkNodes = new int[nNodes];
            Arrays.fill(subnetworkNodes, -1);
            for (i = 0; i < nodes.length; i++)
                subnetworkNodes[nodes[i]] = i;

            subnetwork.nEdges = 0;
            subnetwork.nodeWeights = new double[subnetwork.nNodes];
            subnetwork.firstNeighborIndices = new long[subnetwork.nNodes + 1];
            subnetworkNeighbors = new LargeIntArray(nEdges);
            subnetworkEdgeWeights = new LargeDoubleArray(nEdges);
            for (i = 0; i < subnetwork.nNodes; i++)
            {
                j = nodes[i];
                subnetwork.nodeWeights[i] = nodeWeights[j];
                for (k = firstNeighborIndices[j]; k < firstNeighborIndices[j + 1]; k++)
                    if (subnetworkNodes[neighbors.get(k)] >= 0)
                    {
                        subnetworkNeighbors.set(subnetwork.nEdges,
                                                subnetworkNodes[neighbors.get(k)]);
                        subnetworkEdgeWeights.set(subnetwork.nEdges,
                                                  edgeWeights.get(k));
                        subnetwork.nEdges++;
                    }
                subnetwork.firstNeighborIndices[i + 1] = subnetwork.nEdges;
            }
            subnetwork.neighbors = subnetworkNeighbors.copyOfRange(0, subnetwork.nEdges);
            subnetwork.edgeWeights = subnetworkEdgeWeights.copyOfRange(0, subnetwork.nEdges);
        }

        subnetwork.totalEdgeWeightSelfLinks = 0;

        return subnetwork;
    }

    /**
     * Creates an induced subnetwork for specified nodes.
     *
     * @param nodesInSubnetwork Indicates the nodes to be included in the
     *                          subnetwork.
     *
     * @return Subnetwork
     */
    public Network createSubnetwork(boolean[] nodesInSubnetwork)
    {
        int i, j;
        int[] nodes;

        i = 0;
        for (j = 0; j < nNodes; j++)
            if (nodesInSubnetwork[j])
                i++;
        nodes = new int[i];
        i = 0;
        for (j = 0; j < nNodes; j++)
            if (nodesInSubnetwork[j])
            {
                nodes[i] = j;
                i++;
            }
        return createSubnetwork(nodes);
    }

    /**
     * Creates an induced subnetwork for a specified cluster in a clustering.
     *
     * <p>
     * If subnetworks need to be created for all clusters in a clustering, it
     * is more efficient to use {@link #createSubnetworks(Clustering
     * clustering)}.
     * </p>
     *
     * @param clustering Clustering
     * @param cluster    Cluster
     *
     * @return Subnetwork
     */
    public Network createSubnetwork(Clustering clustering, int cluster)
    {
        LargeDoubleArray subnetworkEdgeWeights;
        LargeIntArray subnetworkNeighbors;
        int[] subnetworkNodes;
        int[][] nodesPerCluster;

        nodesPerCluster = clustering.getNodesPerCluster();
        subnetworkNodes = new int[nNodes];
        subnetworkNeighbors = new LargeIntArray(nEdges);
        subnetworkEdgeWeights = new LargeDoubleArray(nEdges);
        return createSubnetwork(clustering, cluster, nodesPerCluster[cluster], subnetworkNodes, subnetworkNeighbors, subnetworkEdgeWeights);
    }

    /**
     * Creates induced subnetworks for the clusters in a clustering.
     *
     * @param clustering Clustering
     *
     * @return Subnetworks
     */
    public Network[] createSubnetworks(Clustering clustering)
    {
        LargeDoubleArray subnetworkEdgeWeights;
        int i;
        int[] subnetworkNodes;
        LargeIntArray subnetworkNeighbors;
        int[][] nodesPerCluster;
        Network[] subnetworks;

        subnetworks = new Network[clustering.nClusters];
        nodesPerCluster = clustering.getNodesPerCluster();
        subnetworkNodes = new int[nNodes];
        subnetworkNeighbors = new LargeIntArray(nEdges);
        subnetworkEdgeWeights = new LargeDoubleArray(nEdges);
        for (i = 0; i < clustering.nClusters; i++)
            subnetworks[i] = createSubnetwork(clustering, i, nodesPerCluster[i], subnetworkNodes, subnetworkNeighbors, subnetworkEdgeWeights);
        return subnetworks;
    }

    /**
     * Creates an induced subnetwork of the largest connected component.
     *
     * @return Subnetwork
     */
    public Network createSubnetworkLargestComponent()
    {
        return createSubnetwork(identifyComponents(), 0);
    }

    /**
     * Creates a reduced (or aggregate) network based on a clustering.
     *
     * <p>
     * Each node in the reduced network corresponds to a cluster of nodes in
     * the original network. The weight of a node in the reduced network equals
     * the sum of the weights of the nodes in the corresponding cluster in the
     * original network. The weight of an edge between two nodes in the reduced
     * network equals the sum of the weights of the edges between the nodes in
     * the two corresponding clusters in the original network.
     * </p>
     *
     * @param clustering Clustering
     *
     * @return Reduced network
     */
    public Network createReducedNetwork(Clustering clustering)
    {
        LargeDoubleArray reducedNetworkEdgeWeights1, reducedNetworkEdgeWeights2;
        int i, j, k, l, n;
        long m;
        LargeIntArray reducedNetworkNeighbors1, reducedNetworkNeighbors2;
        int[][] nodesPerCluster;
        Network reducedNetwork;

        reducedNetwork = new Network();

        reducedNetwork.nNodes = clustering.nClusters;

        reducedNetwork.nEdges = 0;
        reducedNetwork.nodeWeights = new double[clustering.nClusters];
        reducedNetwork.firstNeighborIndices = new long[clustering.nClusters + 1];
        reducedNetwork.totalEdgeWeightSelfLinks = totalEdgeWeightSelfLinks;
        reducedNetworkNeighbors1 = new LargeIntArray(nEdges);
        reducedNetworkEdgeWeights1 = new LargeDoubleArray(nEdges);
        reducedNetworkNeighbors2 = new LargeIntArray(clustering.nClusters - 1);
        reducedNetworkEdgeWeights2 = new LargeDoubleArray(clustering.nClusters);
        nodesPerCluster = clustering.getNodesPerCluster();
        for (i = 0; i < clustering.nClusters; i++)
        {
            j = 0;
            for (k = 0; k < nodesPerCluster[i].length; k++)
            {
                l = nodesPerCluster[i][k];

                reducedNetwork.nodeWeights[i] += nodeWeights[l];

                for (m = firstNeighborIndices[l]; m < firstNeighborIndices[l + 1]; m++)
                {
                    n = clustering.clusters[neighbors.get(m)];
                    if (n != i)
                    {
                        if (reducedNetworkEdgeWeights2.get(n) == 0)
                        {
                            reducedNetworkNeighbors2.set(j, n);
                            j++;
                        }
                        reducedNetworkEdgeWeights2.add(n, edgeWeights.get(m));
                    }
                    else
                        reducedNetwork.totalEdgeWeightSelfLinks += edgeWeights.get(m);
                }
            }

            for (k = 0; k < j; k++)
            {
                reducedNetworkNeighbors1.set(reducedNetwork.nEdges + k, reducedNetworkNeighbors2.get(k));
                reducedNetworkEdgeWeights1.set(reducedNetwork.nEdges + k, reducedNetworkEdgeWeights2.get(reducedNetworkNeighbors2.get(k)));
                reducedNetworkEdgeWeights2.set(reducedNetworkNeighbors2.get(k), 0);
            }
            reducedNetwork.nEdges += j;
            reducedNetwork.firstNeighborIndices[i + 1] = reducedNetwork.nEdges;
        }
        reducedNetwork.neighbors = reducedNetworkNeighbors1.copyOfRange(0, reducedNetwork.nEdges);
        reducedNetwork.edgeWeights = reducedNetworkEdgeWeights1.copyOfRange(0, reducedNetwork.nEdges);

        return reducedNetwork;
    }

    /**
     * Identifies the connected components of the network.
     *
     * @return Connected components
     */
    public Clustering identifyComponents()
    {
        ComponentsAlgorithm componentsAlgorithm;

        componentsAlgorithm = new ComponentsAlgorithm();
        return componentsAlgorithm.findClustering(this);
    }

    /**
     * Checks the integrity of the network.
     *
     * <p>
     * It is checked whether:
     * </p>
     *
     * <ul>
     * <li>variables have a correct value,</li>
     * <li>arrays have a correct length,</li>
     * <li>edges are sorted correctly,</li>
     * <li>edges are stored in both directions.</li>
     * </ul>
     *
     * <p>
     * An exception is thrown if the integrity of the network is violated.
     * </p>
     *
     * @throws IllegalArgumentException An illegal argument was provided in the
     *                                  construction of the network.
     */
    public void checkIntegrity() throws IllegalArgumentException
    {
        LargeBooleanArray checked;
        int i, k;
        long j, l;

        // Check whether variables have a correct value and arrays have a
        // correct length.
        if (nNodes < 0)
            throw new IllegalArgumentException("nNodes must be non-negative.");

        if (nEdges < 0)
            throw new IllegalArgumentException("nEdges must be non-negative.");

        if (nEdges % 2 == 1)
            throw new IllegalArgumentException("nEdges must be even.");

        if (nodeWeights.length != nNodes)
            throw new IllegalArgumentException("Length of nodeWeight array must be equal to nNodes.");

        if (firstNeighborIndices.length != nNodes + 1)
            throw new IllegalArgumentException("Length of firstNeighborIndices array must be equal to nNodes + 1.");

        if (firstNeighborIndices[0] != 0)
            throw new IllegalArgumentException("First element of firstNeighborIndices array must be equal to 0.");

        if (firstNeighborIndices[nNodes] != nEdges)
            throw new IllegalArgumentException("Last element of firstNeighborIndices array must be equal to nEdges.");

        if (neighbors.size() != nEdges)
            throw new IllegalArgumentException("Length of neighbors array must be equal to nEdges.");

        if (edgeWeights.size() != nEdges)
            throw new IllegalArgumentException("Length of edgeWeights array must be equal to nEdges.");

        // Check whether edges are sorted correctly.
        for (i = 0; i < nNodes; i++)
        {
            if (firstNeighborIndices[i + 1] < firstNeighborIndices[i])
                throw new IllegalArgumentException("Elements of firstNeighborIndices array must be in non-decreasing order.");

            for (j = firstNeighborIndices[i]; j < firstNeighborIndices[i + 1]; j++)
            {
                k = neighbors.get(j);

                if (k < 0)
                    throw new IllegalArgumentException("Elements of neighbors array must have non-negative values.");
                else if (k >= nNodes)
                    throw new IllegalArgumentException("Elements of neighbors array must have values less than nNodes.");

                if (j > firstNeighborIndices[i])
                {
                    l = neighbors.get(j - 1);
                    if (k < l)
                        throw new IllegalArgumentException("For each node, corresponding elements of neighbors array must be in increasing order.");
                    else if (k == l)
                        throw new IllegalArgumentException("For each node, corresponding elements of neighbors array must not include duplicate values.");
                }
            }
        }

        // Check whether edges are stored in both directions.
        checked = new LargeBooleanArray(nEdges);
        for (i = 0; i < nNodes; i++)
            for (j = firstNeighborIndices[i]; j < firstNeighborIndices[i + 1]; j++)
                if (!checked.get(j))
                {
                    k = neighbors.get(j);

                    l = neighbors.binarySearch(firstNeighborIndices[k], firstNeighborIndices[k+1], i);
                    if (l < 0)
                        throw new IllegalArgumentException("Edges must be stored in both directions.");
                    if (edgeWeights.get(j) != edgeWeights.get(l))
                        throw new IllegalArgumentException("Edge weights must be the same in both directions.");

                    checked.set(j, true);
                    checked.set(l, true);
                }
    }

    public static void sortEdges(LargeIntArray[] edges, LargeDoubleArray edgeWeights)
    {
        class EdgeComparator
        {
            LargeIntArray[] edges;
            LargeLongArray indices;

            public EdgeComparator(LargeIntArray[] edges, LargeLongArray indices)
            {
                this.edges = edges;
                this.indices = indices;
            }

            public int compare(long i, long j)
            {
                long a = indices.get(i);
                long b = indices.get(j);
                if (edges[0].get(a) > edges[0].get(b))
                    return 1;
                if (edges[0].get(a) < edges[0].get(b))
                    return -1;
                if (edges[1].get(a) > edges[1].get(b))
                    return 1;
                if (edges[1].get(a) < edges[1].get(b))
                    return -1;
                return 0;
            }
        }

        LargeDoubleArray edgeWeightsSorted;
        long i, nEdges;
        LargeIntArray[] edgesSorted;
        LargeLongArray indices;

        nEdges = edges[0].size();

        // Determine sorting order.
        indices = new LargeLongArray(nEdges);
        for (i = 0; i < nEdges; i++)
            indices.set(i, i);

        indices.sort(new EdgeComparator(edges, indices)::compare);

        // Sort edges.
        edgesSorted = new LargeIntArray[2];
        edgesSorted[0] = new LargeIntArray(nEdges);
        edgesSorted[1] = new LargeIntArray(nEdges);
        for (i = 0; i < nEdges; i++)
        {
            edgesSorted[0].set(i, edges[0].get(indices.get(i)) );
            edgesSorted[1].set(i, edges[1].get(indices.get(i)) );
        }
        edges[0] = edgesSorted[0];
        edges[1] = edgesSorted[1];

        // Sort edge weights.
        if (edgeWeights != null)
        {
            edgeWeightsSorted = new LargeDoubleArray(nEdges);
            for (i = 0; i < nEdges; i++)
                edgeWeightsSorted.set(i, edgeWeights.get(indices.get(i)) );
            edgeWeights.updateFrom(edgeWeightsSorted);
        }
    }

    private Network()
    {
    }

    private Network(int nNodes, double[] nodeWeights, boolean setNodeWeightsToTotalEdgeWeights, LargeIntArray[] edges, LargeDoubleArray edgeWeights, boolean sortedEdges, boolean checkIntegrity)
    {
        LargeDoubleArray edgeWeights2;
        long i, j;
        int k;
        LargeIntArray[] edges2;

        if (!sortedEdges)
        {
            edges2 = new LargeIntArray[2];
            edges2[0] = new LargeIntArray(2 * edges[0].size());
            edges2[1] = new LargeIntArray(2 * edges[1].size());
            edgeWeights2 = (edgeWeights != null) ? new LargeDoubleArray(2 * edges[0].size()) : null;
            i = 0;
            for (j = 0; j < edges[0].size(); j++)
            {
                edges2[0].set(i, edges[0].get(j));
                edges2[1].set(i, edges[1].get(j));
                if (edgeWeights != null)
                    edgeWeights2.set(i, edgeWeights.get(j));
                i++;
                if (edges[0].get(j) != edges[1].get(j))
                {
                    edges2[0].set(i, edges[1].get(j));
                    edges2[1].set(i, edges[0].get(j));
                    if (edgeWeights != null)
                        edgeWeights2.set(i, edgeWeights.get(j));
                    i++;
                }
            }
            edges = edges2;
            if (edgeWeights != null)
                edgeWeights = edgeWeights2;
            sortEdges(edges, edgeWeights);
        }

        this.nNodes = nNodes;
        nEdges = 0;
        firstNeighborIndices = new long[nNodes + 1];
        neighbors = new LargeIntArray(edges[0].size());
        this.edgeWeights = new LargeDoubleArray(edges[0].size());
        totalEdgeWeightSelfLinks = 0;
        k = 1;
        for (j = 0; j < edges[0].size(); j++)
            if (edges[0].get(j) != edges[1].get(j))
            {
                for (; k <= edges[0].get(j); k++)
                    firstNeighborIndices[k] = nEdges;
                neighbors.set(nEdges, edges[1].get(j));
                this.edgeWeights.set(nEdges, (edgeWeights != null) ? edgeWeights.get(j) : 1);
                nEdges++;
            }
            else
                totalEdgeWeightSelfLinks += (edgeWeights != null) ? edgeWeights.get(j) : 1;
        for (; k <= nNodes; k++)
            firstNeighborIndices[k] = nEdges;
	this.neighbors.resize(nEdges); this.neighbors.shrink();
        this.edgeWeights.resize(nEdges); this.edgeWeights.shrink();

        this.nodeWeights = (nodeWeights != null) ? nodeWeights.clone() : (setNodeWeightsToTotalEdgeWeights ? getTotalEdgeWeightPerNodeHelper() : nl.cwts.util.Arrays.createDoubleArrayOfOnes(nNodes));

        if (checkIntegrity)
            checkIntegrity();
    }

    private Network(int nNodes, double[] nodeWeights, boolean setNodeWeightsToTotalEdgeWeights, long[] firstNeighborIndices, LargeIntArray neighbors, LargeDoubleArray edgeWeights, boolean checkIntegrity)
    {
        this.nNodes = nNodes;
        nEdges = neighbors.size();
        this.firstNeighborIndices = firstNeighborIndices.clone();
        this.neighbors = neighbors.clone();
        this.edgeWeights = (edgeWeights != null) ? edgeWeights.clone() :
                new LargeDoubleArray(nEdges, 1);
        totalEdgeWeightSelfLinks = 0;

        this.nodeWeights = (nodeWeights != null) ? nodeWeights.clone() : (setNodeWeightsToTotalEdgeWeights ? getTotalEdgeWeightPerNodeHelper() : nl.cwts.util.Arrays.createDoubleArrayOfOnes(nNodes));

        if (checkIntegrity)
            checkIntegrity();
    }

    private double[] getTotalEdgeWeightPerNodeHelper()
    {
        double[] totalEdgeWeightPerNode;
        int i;

        totalEdgeWeightPerNode = new double[nNodes];
        for (i = 0; i < nNodes; i++)
            totalEdgeWeightPerNode[i] = edgeWeights.calcSum(firstNeighborIndices[i], firstNeighborIndices[i + 1]);
        return totalEdgeWeightPerNode;
    }

    private double getRandomNumber(int node1, int node2, LargeDoubleArray randomNumbers)
    {
        int i, j;

        if (node1 < node2)
        {
            i = node1;
            j = node2;
        }
        else
        {
            i = node2;
            j = node1;
        }
        return randomNumbers.get(i * nNodes + j);
    }

    protected Network createSubnetwork(Clustering clustering, int cluster, int[] nodes, int[] subnetworkNodes, LargeIntArray subnetworkNeighbors, LargeDoubleArray subnetworkEdgeWeights)
    {
        int i, j;
        long k;
        Network subnetwork;

        subnetwork = new Network();

        subnetwork.nNodes = nodes.length;

        if (subnetwork.nNodes == 1)
        {
            subnetwork.nEdges = 0;
            subnetwork.nodeWeights = new double[1];
            subnetwork.nodeWeights[0] = nodeWeights[nodes[0]];
            subnetwork.firstNeighborIndices = new long[2];
            subnetwork.neighbors = new LargeIntArray(0);
            subnetwork.edgeWeights = new LargeDoubleArray(0);
        }
        else
        {
            for (i = 0; i < nodes.length; i++)
                subnetworkNodes[nodes[i]] = i;

            subnetwork.nEdges = 0;
            subnetwork.nodeWeights = new double[subnetwork.nNodes];
            subnetwork.firstNeighborIndices = new long[subnetwork.nNodes + 1];
            for (i = 0; i < subnetwork.nNodes; i++)
            {
                j = nodes[i];
                subnetwork.nodeWeights[i] = nodeWeights[j];
                for (k = firstNeighborIndices[j]; k < firstNeighborIndices[j + 1]; k++)
                    if (clustering.clusters[neighbors.get(k)] == cluster)
                    {
                        subnetworkNeighbors.set(subnetwork.nEdges,
                                                subnetworkNodes[neighbors.get(k)]);
                        subnetworkEdgeWeights.set(subnetwork.nEdges,
                                                  edgeWeights.get(k));
                        subnetwork.nEdges++;
                    }
                subnetwork.firstNeighborIndices[i + 1] = subnetwork.nEdges;
            }
            subnetwork.neighbors = subnetworkNeighbors.copyOfRange(0, subnetwork.nEdges);
            subnetwork.edgeWeights = subnetworkEdgeWeights.copyOfRange(0, subnetwork.nEdges);
        }

        subnetwork.totalEdgeWeightSelfLinks = 0;

        return subnetwork;
    }

    public class RangeIterable implements Iterable<Long>
    {
        long from;
        long to;

        public RangeIterable(long from, long to)
        {
            this.from = from;
            this.to = to;
        }

        @Override
        public java.util.Iterator<Long> iterator()
        {
            return new RangeIterator(from, to);
        }
    }

    public class RangeIterator implements PrimitiveIterator.OfLong
    {
        private final long to;
        private long current;

        public RangeIterator(long from, long to)
        {
            this.current = from;
            this.to = to;
        }

        @Override
        public long nextLong()
        {
            return current++;
        }

        @Override
        public boolean hasNext()
        {
            return current < to;
        }
    }
}
