package cwts.networkanalysis;

import cwts.util.DynamicDoubleArray;
import cwts.util.DynamicIntArray;

import java.util.List;
import java.util.Random;

public class AlgorithmWrapper {
    private boolean useModularity;
    private double resolution;
    private boolean useLouvain;
    private int iterations;

    public AlgorithmWrapper(boolean useLouvain, double resolution, boolean useModularity, int iterations) {
        this.useLouvain = useLouvain;
        this.resolution = resolution;
        this.useModularity = useModularity;
        this.iterations = iterations;
    }

    public Clustering process(List<List> data) {
        DynamicDoubleArray edgeWeights = new DynamicDoubleArray(100);

        DynamicIntArray[] edges = new DynamicIntArray[2];
        edges[0] = new DynamicIntArray(100);
        edges[1] = new DynamicIntArray(100);

        int nNodes = 0;

        for(List list: data) {
            int node1 = (Integer)list.get(0);
            int node2 = (Integer)list.get(1);

            edges[0].append(node1);
            edges[1].append(node2);
            edgeWeights.append((Double)list.get(2));

            if (node1 >= nNodes)
                nNodes = node1 + 1;
            if (node2 >= nNodes)
                nNodes = node2 + 1;
        }

        int[][] edges2 = new int[2][];
        edges2[0] = edges[0].toArray();
        edges2[1] = edges[1].toArray();

        double[] edgeWeights2 = edgeWeights.toArray();

        Network network = new Network(nNodes, useModularity, edges2, edgeWeights2, false, true);

        Clustering initialClustering = new Clustering(network.getNNodes());

        double newResolution = useModularity ? (resolution / (2 * network.getTotalEdgeWeight() + network.getTotalEdgeWeightSelfLinks())) : resolution;
        //Random random = useSeed ? new Random(seed) : new Random();
        IterativeCPMClusteringAlgorithm algorithm = useLouvain ?
                new LouvainAlgorithm(newResolution, this.iterations, new Random()) :
                new LeidenAlgorithm(newResolution, this.iterations, 1e-2, new Random());

        Clustering finalClustering = null;
        double maxQuality = Double.NEGATIVE_INFINITY;

        Clustering clustering = initialClustering.clone();

        //for (int i = 0; i < 1; i++) {
        int index = 1;

        while (true) {
            //Clustering clustering = initialClustering.clone();
            algorithm.improveClustering(network, clustering);
            double quality = algorithm.calcQuality(network, clustering);

            System.out.println("Iteration: " + index + " -> " + quality + ", " + maxQuality);

            if (quality > maxQuality) {
                finalClustering = clustering;
                maxQuality = quality;
            }
            else {
                break;
            }

            index++;
        }

        return finalClustering;
    }
}
