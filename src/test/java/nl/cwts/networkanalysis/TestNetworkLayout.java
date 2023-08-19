package nl.cwts.networkanalysis;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.cwts.util.LargeDoubleArray;
import nl.cwts.util.LargeIntArray;

public class TestNetworkLayout
{
    Network testNetwork;

    @BeforeEach
    public void setUp()
    {
        int[][] edges = { { 0, 0, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 6, 6, 7 }, { 1, 9, 4, 6, 9, 5, 7, 8, 9, 4, 5, 7, 8, 9, 5, 6, 8, 9, 6, 7, 8, 7, 9, 8 } };
        double[] edgeWeights = { 16, 90, 1, 1, 82, 2, 13, 72, 5, 13, 1, 2, 2, 2, 5, 15, 4, 2, 139, 6, 7, 4, 1, 15 };
        LargeIntArray[] edges2 = new LargeIntArray[2];
        edges2[0] = new LargeIntArray(edges[0]);
        edges2[1] = new LargeIntArray(edges[1]);
        LargeDoubleArray edgeWeights2 = new LargeDoubleArray(edgeWeights);
        testNetwork = new Network(10, true, edges2, edgeWeights2, false, true);
    }

    @Test
    public void testTotalEdgeWeightAfterNormalization()
    {
        Network normalizedNetworkNoNormalization = testNetwork.createNetworkWithoutNodeWeights();
        assertEquals(500, testNetwork.getTotalEdgeWeight(), 1e-10);
        assertEquals(500, normalizedNetworkNoNormalization.getTotalEdgeWeight(), 1e-10);

        Network normalizedNetworkAssociationStrength = testNetwork.createNormalizedNetworkUsingAssociationStrength();
        assertEquals(500, testNetwork.getTotalEdgeWeight(), 1e-10);
        assertEquals(500, normalizedNetworkNoNormalization.getTotalEdgeWeight(), 1e-10);
        assertEquals(59.01145827229578, normalizedNetworkAssociationStrength.getTotalEdgeWeight(), 1e-10);

        Network normalizedNetworkFractionalization = testNetwork.createNormalizedNetworkUsingFractionalization();
        assertEquals(500, testNetwork.getTotalEdgeWeight(), 1e-10);
        assertEquals(500, normalizedNetworkNoNormalization.getTotalEdgeWeight(), 1e-10);
        assertEquals(59.01145827229578, normalizedNetworkAssociationStrength.getTotalEdgeWeight(), 1e-10);
        assertEquals(50, normalizedNetworkFractionalization.getTotalEdgeWeight(), 1e-10);
    }

    @Test
    public void testGradientDescentVOSLayoutAlgorithm()
    {
        int attraction = 2;
        int repulsion = -2;
        GradientDescentVOSLayoutAlgorithm algorithm = new GradientDescentVOSLayoutAlgorithm(attraction, repulsion, 0, new Random(0));
        testNetwork = testNetwork.createNormalizedNetworkUsingAssociationStrength();
        Layout layout = algorithm.findLayout(testNetwork);
        layout.standardize(true);

        Layout expectedLayout = new Layout(new double[][] { { 1.136341835, 0.879152105, -0.512331857, -0.032236745, -0.146102352, -0.528511035, -0.754004637, -0.542296378, -0.22047403, 0.720463094 }, { -0.149931232, 0.318787053, -0.661664094, 0.007739771, 0.280012379, 0.612571137, 0.328330118, -0.205903696, -0.460960297, -0.068981139 } });
        for (int i = 0; i < testNetwork.getNNodes(); i++)
            assertArrayEquals(expectedLayout.getCoordinates(i), layout.getCoordinates(i), 1e-8);
        assertEquals(55.06823414047805, algorithm.calcQuality(testNetwork, layout), 1e-10);
    }
}
