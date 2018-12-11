package cwts.networkanalysis;

import cwts.util.Arrays;
import java.util.Random;

/**
 * Fast local moving algorithm.
 *
 * <p>
 * The fast local moving algorithm first adds all nodes in a network to a
 * queue. It then removes a node from the queue. The node is moved to the
 * cluster that results in the largest increase in the quality function. If the
 * current cluster assignment of the node is already optimal, the node is not
 * moved. If the node is moved to a different cluster, the neighbors of the
 * node that do not belong to the node's new cluster and that are not yet in
 * the queue are added to the queue. The algorithm continues removing nodes
 * from the queue until the queue is empty.
 * </p>
 *
 * <p>
 * The fast local moving algorithm provides a fast variant of the {@link
 * StandardLocalMovingAlgorithm}.
 * </p>
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 * @author Vincent Traag
 */
public class FastLocalMovingAlgorithmParallel extends IterativeCPMClusteringAlgorithm
{
    /**
     * Random number generator.
     */
    protected Random random;

    /**
     * Constructs a fast local moving algorithm.
     */
    public FastLocalMovingAlgorithmParallel()
    {
        this(new Random());
    }

    /**
     * Constructs a fast local moving algorithm.
     *
     * @param random Random number generator
     */
    public FastLocalMovingAlgorithmParallel(Random random)
    {
        this(DEFAULT_RESOLUTION, DEFAULT_N_ITERATIONS, random);
    }

    /**
     * Constructs a fast local moving algorithm for a specified resolution
     * parameter and number of iterations.
     *
     * @param resolution  Resolution parameter
     * @param nIterations Number of iterations
     * @param random      Random number generator
     */
    public FastLocalMovingAlgorithmParallel(double resolution, int nIterations, Random random)
    {
        super(resolution, nIterations);

        this.random = random;
    }

    /**
     * Improves a clustering by performing one iteration of the fast local
     * moving algorithm.
     *
     * <p>
     * The fast local moving algorithm first adds all nodes in a network to a
     * queue. It then removes a node from the queue. The node is moved to the
     * cluster that results in the largest increase in the quality function. If
     * the current cluster assignment of the node is already optimal, the node
     * is not moved. If the node is moved to a different cluster, the neighbors
     * of the node that do not belong to the node's new cluster and that are
     * not yet in the queue are added to the queue. The algorithm continues
     * removing nodes from the queue until the queue is empty.
     * </p>
     *
     * @param network    Network
     * @param clustering Clustering
     *
     * @return Boolean indicating whether the clustering has been improved
     */
    protected boolean improveClusteringOneIteration(Network network, Clustering clustering)
    {
        boolean update;
        boolean[] stableNodes;
        double maxQualityValueIncrement, qualityValueIncrement;
        double[] clusterWeights, edgeWeightPerCluster;
        int bestCluster, currentCluster, i, j, k, l, nNeighboringClusters, nUnstableNodes, nUnusedClusters;
        int[] neighboringClusters, nNodesPerCluster, nodeOrder, unusedClusters;

        if (network.nNodes == 1)
            return false;

        update = false;

        clusterWeights = new double[network.nNodes];
        nNodesPerCluster = new int[network.nNodes];
        for (i = 0; i < network.nNodes; i++)
        {
            clusterWeights[clustering.clusters[i]] += network.nodeWeights[i];
            nNodesPerCluster[clustering.clusters[i]]++;
        }

        nUnusedClusters = 1;
        unusedClusters = new int[network.nNodes];
        for (i = network.nNodes - 1; i >= 0; i--)
            if (nNodesPerCluster[i] == 0)
            {
                unusedClusters[nUnusedClusters] = i;
                nUnusedClusters++;
            }

        nodeOrder = Arrays.generateRandomPermutation(network.nNodes, random);

        /*
         * Iterate over the nodeOrder array in a cyclical manner. When the end
         * of the array has been reached, start again from the beginning. The
         * queue of nodes that still need to be visited is given by
         * nodeOrder[i], ..., nodeOrder[i + nUnstableNodes - 1]. Continue
         * iterating until the queue is empty.
         */
        edgeWeightPerCluster = new double[network.nNodes];
        neighboringClusters = new int[network.nNodes];
        stableNodes = new boolean[network.nNodes];
        nUnstableNodes = network.nNodes;

        ClusterDataManager clusterDataManager = new ClusterDataManager(network, clustering, stableNodes, clusterWeights, unusedClusters, nodeOrder, nNodesPerCluster, nUnstableNodes, nUnusedClusters);

        i = 0;
        do
        {
            j = nodeOrder[i];

            currentCluster = clustering.clusters[j];

            

            /*
             * Identify the neighboring clusters of the currently selected
             * node, that is, the clusters with which the currently selected
             * node is connected. An empty cluster is also included in the set
             * of neighboring clusters. In this way, it is always possible that
             * the currently selected node will be moved to an empty cluster.
             */
            //nNeighboringClusters = 0;

            neighboringClusters[0] = unusedClusters[clusterDataManager.getnUnusedClusters() - 1];
            nNeighboringClusters = 1;
            
            for (k = network.firstNeighborIndices[j]; k < network.firstNeighborIndices[j + 1]; k++)
            {
                l = clustering.clusters[network.neighbors[k]];

                if (edgeWeightPerCluster[l] == 0)
                {
                    neighboringClusters[nNeighboringClusters] = l;
                    nNeighboringClusters++;
                }
                edgeWeightPerCluster[l] += network.edgeWeights[k];
            }

            /*
             * For each neighboring cluster of the currently selected node,
             * calculate the increment of the quality function obtained by
             * moving the currently selected node to the neighboring cluster.
             * Determine the neighboring cluster for which the increment of the
             * quality function is largest. The currently selected node will be
             * moved to this optimal cluster. In order to guarantee convergence
             * of the algorithm, if the old cluster of the currently selected
             * node is optimal but there are also other optimal clusters, the
             * currently selected node will be moved back to its old cluster.
             */
            bestCluster = currentCluster;
            maxQualityValueIncrement = edgeWeightPerCluster[currentCluster] - network.nodeWeights[j] * (clusterWeights[currentCluster]-network.nodeWeights[j]) * resolution;
            for (k = 0; k < nNeighboringClusters; k++)
            {
                l = neighboringClusters[k];
                qualityValueIncrement = edgeWeightPerCluster[l] - network.nodeWeights[j] * clusterWeights[l] * resolution;
                if (qualityValueIncrement > maxQualityValueIncrement)
                {
                    bestCluster = l;
                    maxQualityValueIncrement = qualityValueIncrement;
                }

                edgeWeightPerCluster[l] = 0;
            }
            

            /*
             * Mark the currently selected node as stable and remove it from
             * the queue.
             */
            clusterDataManager.markDone(j);
            nUnstableNodes = clusterDataManager.getnUnstableNodes();

            /*
             * If the new cluster of the currently selected node is different
             * from the old cluster, some further updating of the clustering
             * statistics is performed. Also, the neighbors of the currently
             * selected node that do not belong to the new cluster are marked
             * as unstable and are added to the queue.
             */
            if (bestCluster != currentCluster)
            {
                
                clusterDataManager.moveNode(currentCluster, bestCluster, i, j);
                update = true;
            }

            i = (i < network.nNodes - 1) ? (i + 1) : 0;
        } while (nUnstableNodes > 0);

        if (update)
            clustering.removeEmptyClusters();

        return update;
    }
}

class ClusterDataManager {
    Network network;
    Clustering clustering;
    boolean[] stableNodes;
    double[] clusterWeights;
    int[] unusedClusters, nodeOrder, nNodesPerCluster;
    int nUnstableNodes, nUnusedClusters;

    public ClusterDataManager (Network network, Clustering clustering, boolean[] stableNodes, double[] clusterWeights, int[] unusedClusters, int[] nodeOrder, int[] nNodesPerCluster, int nUnstableNodes, int nUnusedClusters) {
        this.network = network;
        this.clustering = clustering;
        this.stableNodes = stableNodes;
        this.clusterWeights = clusterWeights;
        this.unusedClusters = unusedClusters;
        this.nodeOrder = nodeOrder;
        this.nNodesPerCluster = nNodesPerCluster;
        this.nUnstableNodes = nUnstableNodes;
        this.nUnusedClusters = nUnusedClusters;
    }

    public void markDone (int node) {
        stableNodes[node] = true;
        nUnstableNodes--;
    }

    public int getnUnstableNodes () {
        return nUnstableNodes;
    }

    public int getnUnusedClusters () {
        return nUnusedClusters;
    }

    public void moveNode(int clusterA, int clusterB, int i, int j) {
        clusterWeights[clusterA] -= network.nodeWeights[j];
        nNodesPerCluster[clusterA]--;
        clusterWeights[clusterB] += network.nodeWeights[j];
        nNodesPerCluster[clusterB]++;
        if (nUnusedClusters > 0 && nNodesPerCluster[clusterA] == 0 && clusterB == unusedClusters[nUnusedClusters - 1])
        {
            unusedClusters[nUnusedClusters - 1] = clusterA;
        }
        else if (nNodesPerCluster[clusterA] == 0)
        {
            unusedClusters[nUnusedClusters] = clusterA;
            nUnusedClusters++;
        }
        else if (clusterB == unusedClusters[nUnusedClusters - 1])
        {
            nUnusedClusters--;
        }
        
        

        clustering.clusters[j] = clusterB;
        if (clusterB >= clustering.nClusters)
            clustering.nClusters = clusterB + 1;

        for (int k = network.firstNeighborIndices[j]; k < network.firstNeighborIndices[j + 1]; k++)
            if (stableNodes[network.neighbors[k]] && (clustering.clusters[network.neighbors[k]] != clusterB))
            {
                stableNodes[network.neighbors[k]] = false;
                nUnstableNodes++;
                nodeOrder[(i + nUnstableNodes < network.nNodes) ? (i + nUnstableNodes) : (i + nUnstableNodes - network.nNodes)] = network.neighbors[k];
            }
    }
}