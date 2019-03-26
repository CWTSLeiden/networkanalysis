package cwts.networkanalysis;

import cwts.util.Arrays;
import java.util.Random;
import java.util.Set;

public class ClusterDataManager {
    Network network;
    Clustering clustering;
    Random random;
    double[] clusterWeights;
    int[] unusedClusters, nodeOrder, nNodesPerCluster;
    int nUnusedClusters;
    int nextNode = 0;
    boolean someThingChanged;
    GeertensIntList taskQueue;

    public ClusterDataManager (Network network, Clustering clustering, GeertensIntList taskQueue) {
        this.network = network;
        this.clustering = clustering;
        this.random = random;
        this.taskQueue = taskQueue;

        initialize();
    }

    private void initialize () {
        someThingChanged = false;

        clusterWeights = new double[network.nNodes];
        nNodesPerCluster = new int[network.nNodes];
        for (int i = 0; i < network.nNodes; i++)
        {
            clusterWeights[clustering.clusters[i]] += network.nodeWeights[i];
            nNodesPerCluster[clustering.clusters[i]]++;
        }

        nUnusedClusters = 1;

        unusedClusters = new int[network.nNodes];
        for (int i = network.nNodes - 1; i >= 0; i--)
            if (nNodesPerCluster[i] == 0)
            {
                unusedClusters[nUnusedClusters] = i;
                nUnusedClusters++;
            }
    }

    public double[] getClusterWeights () {
        return clusterWeights;
    }

    public int[] getnNodesPerCluster () {
        return nNodesPerCluster;
    }

    // public synchronized void markDone (int node) {
    //     stableNodes[node] = true;
    //     nUnstableNodes--;
    // }

    public int getNextUnusedCluster () {
        return unusedClusters[nUnusedClusters - 1];
    }

    public boolean getSomeThingChanged () {
        return someThingChanged;
    }

    // public synchronized QueueElement getNextUnstableNode () {
    //     int nodeNumber = nodeOrder[nextNode];
    //     QueueElement node = new QueueElement(nextNode, nodeNumber);
    //     nextNode = (nextNode < network.nNodes - 1) ? (nextNode + 1) : 0;
    //     return node;
    // }

    public void moveNode(int clusterA, int clusterB, int j) {
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

        GeertensIntList newQueueElements = new GeertensIntList();

        for (int k = network.firstNeighborIndices[j]; k < network.firstNeighborIndices[j + 1]; k++) {
            if (clustering.clusters[network.neighbors[k]] != clusterB)
            {
                newQueueElements.add(network.neighbors[k]);
            }
        }

        synchronized (taskQueue){
            taskQueue.addAll(newQueueElements);
        }
        
        someThingChanged = true;
    }
}