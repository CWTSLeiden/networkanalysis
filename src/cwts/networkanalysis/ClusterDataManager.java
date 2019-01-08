package cwts.networkanalysis;

import cwts.util.Arrays;
import java.util.Random;

public class ClusterDataManager {
    Network network;
    Clustering clustering;
    Random random;
    boolean[] stableNodes;
    double[] clusterWeights;
    int[] unusedClusters, nodeOrder, nNodesPerCluster;
    int nUnstableNodes, nUnusedClusters;
    int nextNode = 0;

    public ClusterDataManager (Network network, Clustering clustering, Random random) {
        this.network = network;
        this.clustering = clustering;
        this.random = random;

        initialize();
    }

    private void initialize () {
        stableNodes = new boolean[network.nNodes];
        nUnstableNodes = network.nNodes;

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

        nodeOrder = Arrays.generateRandomPermutation(network.nNodes, random);
    }

    public double[] getClusterWeights () {
        return clusterWeights;
    }

    public int[] getnNodesPerCluster () {
        return nNodesPerCluster;
    }

    public double[] getClusterWeightsCopy () {
        return clusterWeights.clone();
    }

    public int[] getnNodesPerClusterCopy () {
        return nNodesPerCluster.clone();
    }

    public Clustering getClusteringCopy () {
        return clustering.clone();
    }

    public void markDone (int node) {
        stableNodes[node] = true;
        nUnstableNodes--;
    }

    public int getnUnstableNodes () {
        return nUnstableNodes;
    }

    public int getNextUnusedCluster () {
        return unusedClusters[nUnusedClusters - 1];
    }

    public QueueElement getNextUnstableNode () {
        int nodeNumber = nodeOrder[nextNode];
        QueueElement node = new QueueElement(nextNode, nodeNumber);
        nextNode = (nextNode < network.nNodes - 1) ? (nextNode + 1) : 0;
        return node;
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