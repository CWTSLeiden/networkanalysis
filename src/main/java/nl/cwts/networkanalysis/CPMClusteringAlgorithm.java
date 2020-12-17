package nl.cwts.networkanalysis;

import nl.cwts.util.Arrays;

/**
 * Abstract base class for clustering algorithms that use the CPM quality
 * function.
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 * @author Vincent Traag
 */
public abstract class CPMClusteringAlgorithm implements Cloneable, QualityClusteringAlgorithm
{
    /**
     * Default resolution parameter.
     */
    public static final double DEFAULT_RESOLUTION = 1;

    /**
     * Resolution parameter.
     */
    protected double resolution;

    /**
     * Constructs a CPM clustering algorithm.
     */
    public CPMClusteringAlgorithm()
    {
        this(DEFAULT_RESOLUTION);
    }

    /**
     * Constructs a CPM clustering algorithm with a specified resolution
     * parameter.
     *
     * @param resolution Resolution parameter
     */
    public CPMClusteringAlgorithm(double resolution)
    {
        this.resolution = resolution;
    }

    /**
     * Clones the algorithm.
     *
     * @return Cloned algorithm
     */
    public CPMClusteringAlgorithm clone()
    {
        try
        {
            return (CPMClusteringAlgorithm)super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            return null;
        }
    }

    /**
     * Returns the resolution parameter.
     *
     * @return Resolution parameter
     */
    public double getResolution()
    {
        return resolution;
    }

    /**
     * Sets the resolution parameter.
     *
     * @param resolution Resolution parameter
     */
    public void setResolution(double resolution)
    {
        this.resolution = resolution;
    }

    /**
     * Calculates the quality of a clustering using the CPM quality function.
     *
     * <p>
     * The CPM quality function is given by
     * </p>
     *
     * <blockquote>
     * {@code 1 / (2 * m) * sum(d(c[i], c[j]) * (a[i][j] - resolution * n[i] *
     * n[j]))},
     * </blockquote>
     *
     * <p>
     * where {@code a[i][j]} is the weight of the edge between nodes {@code i}
     * and {@code j}, {@code n[i]} is the weight of node {@code i}, {@code m}
     * is the total edge weight, and {@code resolution} is the resolution
     * parameter. The function {@code d(c[i], c[j])} equals 1 if nodes {@code
     * i} and {@code j} belong to the same cluster and 0 otherwise. The sum is
     * taken over all pairs of nodes {@code i} and {@code j}.
     * </p>
     *
     * <p>
     * Modularity can be expressed in terms of CPM by setting {@code n[i]}
     * equal to the total weight of the edges between node {@code i} and its
     * neighbors and by rescaling the resolution parameter by {@code 2 * m}.
     * </p>
     *
     * @param network    Network
     * @param clustering Clustering
     *
     * @return Quality of the clustering
     */
    public double calcQuality(Network network, Clustering clustering)
    {
        double quality;
        double[] clusterWeights;
        int i, j, k;

        quality = 0;

        for (i = 0; i < network.nNodes; i++)
        {
            j = clustering.clusters[i];
            for (k = network.firstNeighborIndices[i]; k < network.firstNeighborIndices[i + 1]; k++)
                if (clustering.clusters[network.neighbors[k]] == j)
                    quality += network.edgeWeights[k];
        }
        quality += network.totalEdgeWeightSelfLinks;

        clusterWeights = clustering.getClusterWeights(network);

        for (i = 0; i < clustering.nClusters; i++)
            quality -= clusterWeights[i] * clusterWeights[i] * resolution;

        quality /= 2 * network.getTotalEdgeWeight() + network.totalEdgeWeightSelfLinks;

        return quality;
    }

    /**
     * Removes a cluster from a clustering by merging the cluster with another
     * cluster. If a cluster has no connections with other clusters, it cannot
     * be removed.
     * 
     * @param network    Network
     * @param clustering Clustering
     * @param cluster    Cluster to be removed
     * 
     * @return Cluster with which the cluster to be removed has been merged, or
     *         -1 if the cluster could not be removed
     */
    public int removeCluster(Network network, Clustering clustering, int cluster)
    {
        double maxQualityFunction, qualityFunction;
        double[] clusterWeights, totalEdgeWeightPerCluster;
        int i, j;

        clusterWeights = clustering.getClusterWeights(network);

        totalEdgeWeightPerCluster = new double[clustering.nClusters];
        for (i = 0; i < network.nNodes; i++)
        {
            if (clustering.clusters[i] == cluster)
                for (j = network.firstNeighborIndices[i]; j < network.firstNeighborIndices[i + 1]; j++)
                    totalEdgeWeightPerCluster[clustering.clusters[network.neighbors[j]]] += network.edgeWeights[j];
        }

        i = -1;
        maxQualityFunction = 0;
        for (j = 0; j < clustering.nClusters; j++)
            if ((j != cluster) && (clusterWeights[j] > 0))
            {
                qualityFunction = totalEdgeWeightPerCluster[j] / clusterWeights[j];
                if (qualityFunction > maxQualityFunction)
                {
                    i = j;
                    maxQualityFunction = qualityFunction;
                }
            }

        if (i >= 0)
        {
            for (j = 0; j < network.nNodes; j++)
                if (clustering.clusters[j] == cluster)
                    clustering.clusters[j] = i;
            if (cluster == clustering.nClusters - 1)
                clustering.nClusters = Arrays.calcMaximum(clustering.clusters) + 1;
        }

        return i;
    }

    /**
     * Removes small clusters from a clustering. Clusters are merged until each
     * cluster contains at least a certain minimum number of nodes.
     * 
     * @param network             Network
     * @param clustering          Clustering
     * @param minNNodesPerCluster Minimum number of nodes per cluster
     * 
     * @return Boolean indicating whether any clusters have been removed
     */
    public boolean removeSmallClustersBasedOnNNodes(Network network, Clustering clustering, int minNNodesPerCluster)
    {
        int i, j, nNodesSmallestCluster;
        int[] nNodesPerCluster;

        Network reducedNetwork = network.createReducedNetwork(clustering);
        Clustering clusteringReducedNetwork = new Clustering(reducedNetwork.nNodes);

        nNodesPerCluster = clustering.getNNodesPerCluster();

        do
        {
            i = -1;
            nNodesSmallestCluster = minNNodesPerCluster;
            for (j = 0; j < clustering.nClusters; j++)
                if ((nNodesPerCluster[j] > 0) && (nNodesPerCluster[j] < nNodesSmallestCluster))
                {
                    i = j;
                    nNodesSmallestCluster = nNodesPerCluster[j];
                }

            if (i >= 0)
            {
                j = removeCluster(reducedNetwork, clusteringReducedNetwork, i);
                if (j >= 0)
                    nNodesPerCluster[j] += nNodesPerCluster[i];
                nNodesPerCluster[i] = 0;
            }
        }
        while (i >= 0);

        clustering.mergeClusters(clusteringReducedNetwork);

        return clusteringReducedNetwork.nClusters < reducedNetwork.nNodes;
    }
    
    /**
     * Removes small clusters from a clustering. Clusters are merged until each
     * cluster has at least a certain minimum total node weight.
     * 
     * <p>
     * The total node weight of a cluster equals the sum of the weights of the
     * nodes belonging to the cluster.
     * </p>
     * 
     * @param network          Network
     * @param clustering       Clustering
     * @param minClusterWeight Minimum total node weight of a cluster
     * 
     * @return Boolean indicating whether any clusters have been removed
     */
    public boolean removeSmallClustersBasedOnWeight(Network network, Clustering clustering, double minClusterWeight)
    {
        double weightSmallestCluster;
        double[] clusterWeights;
        int i, j;

        Network reducedNetwork = network.createReducedNetwork(clustering);
        Clustering clusteringReducedNetwork = new Clustering(reducedNetwork.nNodes);

        clusterWeights = reducedNetwork.nodeWeights.clone();

        do
        {
            i = -1;
            weightSmallestCluster = minClusterWeight;
            for (j = 0; j < clusteringReducedNetwork.nClusters; j++)
                if ((clusterWeights[j] > 0) && (clusterWeights[j] < weightSmallestCluster))
                {
                    i = j;
                    weightSmallestCluster = clusterWeights[j];
                }

            if (i >= 0)
            {
                j = removeCluster(reducedNetwork, clusteringReducedNetwork, i);
                if (j >= 0)
                    clusterWeights[j] += clusterWeights[i];
                clusterWeights[i] = 0;
            }
        }
        while (i >= 0);

        clustering.mergeClusters(clusteringReducedNetwork);

        return clusteringReducedNetwork.nClusters < reducedNetwork.nNodes;
    }
}
