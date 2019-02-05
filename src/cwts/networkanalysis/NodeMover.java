package cwts.networkanalysis;

import java.util.LinkedHashSet;
import java.util.Set;

public class NodeMover extends Thread {
	Set<Integer> taskQueue;
	Network network;
	Clustering clustering;
	ClusterDataManager clusterDataManager;
	double[] clusterWeights, edgeWeightPerCluster;
	double resolution, maxQualityValueIncrement, qualityValueIncrement;
    int[] neighboringClusters;
    int bestCluster, currentCluster, k, l, nNeighboringClusters, node;

	public NodeMover (Set<Integer> taskQueue, Network network, Clustering clustering, ClusterDataManager clusterDataManager, double[] clusterWeights, double resolution) {
		this.taskQueue = taskQueue;
		this.network = network;
		this.clustering = clustering;
		this.clusterDataManager = clusterDataManager;
		this.clusterWeights = clusterWeights;
		this.resolution = resolution;
		this.node = node;
		edgeWeightPerCluster = new double[network.nNodes];
    	neighboringClusters = new int[network.nNodes];
	}

	public void run() {
		while (true) {
			synchronized (taskQueue) {
				if(!taskQueue.isEmpty()) {
					node = taskQueue.iterator().next();
					taskQueue.remove(node);
				}
				else {
					try {
						taskQueue.wait(1);
					} catch (InterruptedException ex) {
						System.out.println(ex);
					}
					if(!taskQueue.isEmpty()) {
						node = taskQueue.iterator().next();
						taskQueue.remove(node);
					}
					else {
						node = 0;
						Thread.currentThread().interrupt();
						return;
					}
				}
			}
			try {
				optimizeNodeCluster();
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

	private void optimizeNodeCluster() {
        currentCluster = clustering.clusters[node];

        identifyNeighbours();

        findBestCluster();

        if (bestCluster != currentCluster)
        {
            clusterDataManager.moveNode(currentCluster, bestCluster, node);
        }
	}

	private void findBestCluster() {
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
	}

	private void identifyNeighbours() {
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
	}
}