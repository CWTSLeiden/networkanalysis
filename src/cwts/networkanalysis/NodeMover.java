package cwts.networkanalysis;

public class NodeMover extends Thread {
	GeertensIntList taskQueue;
	GeertensIntList threadQueue;
	Network network;
	Clustering clustering;
	ClusterDataManager clusterDataManager;
	double[] clusterWeights, edgeWeightPerCluster;
	double resolution, maxQualityValueIncrement, qualityValueIncrement;
    int[] neighboringClusters;
    int bestCluster, currentCluster, k, l, nNeighboringClusters, numberOfWorkers;

	public NodeMover (GeertensIntList taskQueue, Network network, Clustering clustering, ClusterDataManager clusterDataManager, double[] clusterWeights, double resolution, int numberOfWorkers) {
		this.taskQueue = taskQueue;
		this.network = network;
		this.clustering = clustering;
		this.clusterDataManager = clusterDataManager;
		this.clusterWeights = clusterWeights;
		this.resolution = resolution;
		this.numberOfWorkers = numberOfWorkers;
		edgeWeightPerCluster = new double[network.nNodes];
    	neighboringClusters = new int[network.nNodes];
	}

	public void run() {
		while (true) {
			synchronized (taskQueue) {
				if(!taskQueue.isEmpty()) {
					threadQueue = taskQueue.popSubList(numberOfWorkers);
				}
				else {
					return;
				}
			}
			processQueue();
		}
	}

	private void processQueue(){
		while(!threadQueue.isEmpty()){
			optimizeNodeCluster(threadQueue.popInt());
		}
	}

	private void optimizeNodeCluster(int node) {
        currentCluster = clustering.clusters[node];

        identifyNeighbours(node);

        findBestCluster(node);

        clusterDataManager.moveNode(currentCluster, bestCluster, node);
	}

	private void findBestCluster(int node) {
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

	private void identifyNeighbours(int node) {
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