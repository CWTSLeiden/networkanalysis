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
        boolean unstableNodes;

        if (network.nNodes == 1)
            return false;

        update = false;

        ClusterDataManager clusterDataManager = new ClusterDataManager(network, clustering, random);

        double[] clusterWeights = clusterDataManager.getClusterWeights();
        int[] nNodesPerCluster = clusterDataManager.getnNodesPerCluster();
        double[] edgeWeightPerCluster = new double[network.nNodes];
        int[] neighboringClusters = new int[network.nNodes];

        unstableNodes = clusterDataManager.unstableNodes();

        while(unstableNodes)
        {
            
            double maxQualityValueIncrement, qualityValueIncrement;
            int bestCluster, currentCluster, k, l, nNeighboringClusters;
            QueueElement nextNode = clusterDataManager.getNextUnstableNode();
            int i = nextNode.getQueuePosition();
            int j = nextNode.getNodeNumber();

            currentCluster = clustering.clusters[j];

            

            /*
             * Identify the neighboring clusters of the currently selected
             * node, that is, the clusters with which the currently selected
             * node is connected. An empty cluster is also included in the set
             * of neighboring clusters. In this way, it is always possible that
             * the currently selected node will be moved to an empty cluster.
             */

            neighboringClusters[0] = clusterDataManager.getNextUnusedCluster();
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

            unstableNodes = clusterDataManager.unstableNodes();
        }

        if (update)
            clustering.removeEmptyClusters();

        return update;
    }
}