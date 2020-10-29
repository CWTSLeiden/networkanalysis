package nl.cwts.networkanalysis;

import nl.cwts.util.LargeIntArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestNetworkClustering
{
    Network testNetwork;

    @BeforeEach
    public void setUp()
    {
        int[][] e = {{0, 0, 1, 1, 1, 2, 2, 3, 3, 3, 4, 4, 5, 5, 5, 6, 6, 7},
                {1, 2, 0, 2, 3, 0, 1, 1, 4, 5, 3, 5, 3, 4, 6, 5, 7, 6}};
        LargeIntArray[] edges = new LargeIntArray[2];
        edges[0] = new LargeIntArray(e[0]);
        edges[1] = new LargeIntArray(e[1]);
        testNetwork = new Network(8, false, edges, null, true, true);
    }

    @Test
    public void testReducedNetworkQualityValue()
    {
        Clustering clustering = new Clustering(new int[]{1, 1, 1, 0, 0, 0, 0, 0});
        LeidenAlgorithm algorithm = new LeidenAlgorithm();
        double expectedQuality = algorithm.calcQuality(testNetwork,
                                                       clustering);
        Network reducedNetwork = testNetwork.createReducedNetwork(clustering);
        Clustering reducedClustering = new Clustering(reducedNetwork.nNodes);
        double reducedQuality = algorithm.calcQuality(reducedNetwork,
                                                      reducedClustering);
        assertEquals(expectedQuality, reducedQuality, 1e-10);
    }

    @Test
    public void testLeidenAlgorithm()
    {
        double resolution = 0.1;
        LeidenAlgorithm algorithm =
                new LeidenAlgorithm(resolution,
                                    LeidenAlgorithm.DEFAULT_N_ITERATIONS,
                                    LeidenAlgorithm.DEFAULT_RANDOMNESS,
                                    new Random(0));
        Clustering clustering = algorithm.findClustering(testNetwork);
        clustering.orderClustersByNNodes();

        Clustering expectedClustering = new Clustering(new int[]{1, 1, 1, 0, 0, 0, 0, 0});
        assertArrayEquals(expectedClustering.getClusters(), clustering.getClusters());
        assertEquals(algorithm.calcQuality(testNetwork, clustering),
                     (16 - 0.1 * (3 * 3 + 5 * 5)) / 18, 1e-10);

        resolution = 0.5;
        algorithm = new LeidenAlgorithm(resolution,
                                        LeidenAlgorithm.DEFAULT_N_ITERATIONS,
                                        LeidenAlgorithm.DEFAULT_RANDOMNESS,
                                        new Random(0));
        clustering = algorithm.findClustering(testNetwork);
        clustering.orderClustersByNNodes();

        expectedClustering = new Clustering(new int[]{0, 0, 0, 1, 1, 1, 2, 2});
        assertArrayEquals(expectedClustering.getClusters(), clustering.getClusters());
        assertEquals(algorithm.calcQuality(testNetwork, clustering),
                     (14 - resolution * (3 * 3 + 3 * 3 + 2 * 2)) / 18, 1e-10);
    }
}

