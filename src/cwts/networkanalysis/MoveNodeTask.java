package cwts.networkanalysis;

public class MoveNodeTask implements Runnable {
	Network network;
	Clustering clustering;
	ClusterDataManager clusterDataManager;
	double[] clusterWeights;
	int[] nNodesPerCluster;
	double resolution;
	int node;

	public MoveNodeTask (Network network, Clustering clustering, ClusterDataManager clusterDataManager, double[] clusterWeights, int[] nNodesPerCluster, double resolution, int node) {
		this.network = network;
		this.clustering = clustering;
		this.clusterDataManager = clusterDataManager;
		this.clusterWeights = clusterWeights;
		this.nNodesPerCluster = nNodesPerCluster;
		this.resolution = resolution;
		this.node = node;
	}

	@Override
	public void run() {
		long start = System.nanoTime();
		double[] edgeWeightPerCluster = new double[network.nNodes];
        int[] neighboringClusters = new int[network.nNodes];

        double maxQualityValueIncrement, qualityValueIncrement;
        int bestCluster, currentCluster, k, l, nNeighboringClusters;

        currentCluster = clustering.clusters[node];

        

        /*
         * Identify the neighboring clusters of the currently selected
         * node, that is, the clusters with which the currently selected
         * node is connected. An empty cluster is also included in the set
         * of neighboring clusters. In this way, it is always possible that
         * the currently selected node will be moved to an empty cluster.
         */

        neighboringClusters[0] = clusterDataManager.getNextUnusedCluster();
        nNeighboringClusters = 1;
        
        for (k = network.firstNeighborIndices[node]; k < network.firstNeighborIndices[node + 1]; k++)
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
        maxQualityValueIncrement = edgeWeightPerCluster[currentCluster] - network.nodeWeights[node] * (clusterWeights[currentCluster]-network.nodeWeights[node]) * resolution;
        for (k = 0; k < nNeighboringClusters; k++)
        {
            l = neighboringClusters[k];
            qualityValueIncrement = edgeWeightPerCluster[l] - network.nodeWeights[node] * clusterWeights[l] * resolution;
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
        //clusterDataManager.markDone(j);

        /*
         * If the new cluster of the currently selected node is different
         * from the old cluster, some further updating of the clustering
         * statistics is performed. Also, the neighbors of the currently
         * selected node that do not belong to the new cluster are marked
         * as unstable and are added to the queue.
         */
        long endOfCalc = System.nanoTime();
        if (bestCluster != currentCluster)
        {
            clusterDataManager.moveNode(currentCluster, bestCluster, node);
        }
        long end = System.nanoTime();
        System.out.println(Thread.currentThread().getId() + " Calc time:\t\t" + (endOfCalc-start));
        System.out.println(Thread.currentThread().getId() + " Move time:\t\t" + (end-endOfCalc));
	}
}