package cwts.networkanalysis;

import java.util.Arrays;
import java.util.Random;

import cwts.util.FastMath;

/**
 * GradientDescentVOSLayoutAlgorithm
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 */
public class GradientDescentVOSLayoutAlgorithm extends VOSLayoutAlgorithm implements LayoutAlgorithm
{
    public static final int DEFAULT_MAX_N_ITERATIONS = 1000;
    public static final double DEFAULT_INITIAL_STEP_SIZE = 1;
    public static final double DEFAULT_MIN_STEP_SIZE = 0.001;
    public static final double DEFAULT_STEP_SIZE_REDUCTION = 0.75;
    public static final int DEFAULT_REQUIRED_N_QUALITY_FUNTION_IMPROVEMENTS = 5;

    protected int maxNIterations;
    protected double initialStepLength;
    protected double minStepLength;
    protected double stepLengthReduction;
    protected int requiredNQualityFunctionImprovements;
    protected Random random;

    public GradientDescentVOSLayoutAlgorithm()
    {
        this(new Random());
    }

    public GradientDescentVOSLayoutAlgorithm(Random random)
    {
        this(DEFAULT_ATTRACTION, DEFAULT_REPULSION, DEFAULT_EDGE_WEIGHT_INCREMENT, random);
    }

    public GradientDescentVOSLayoutAlgorithm(int attraction, int repulsion, double edgeWeightIncrement, Random random)
    {
        this(attraction, repulsion, edgeWeightIncrement, DEFAULT_MAX_N_ITERATIONS, DEFAULT_INITIAL_STEP_SIZE,
                DEFAULT_MIN_STEP_SIZE, DEFAULT_STEP_SIZE_REDUCTION, DEFAULT_REQUIRED_N_QUALITY_FUNTION_IMPROVEMENTS,
                random);
    }

    public GradientDescentVOSLayoutAlgorithm(int attraction, int repulsion, double edgeWeightIncrement,
            int maxNIterations, double initialStepLength, double minStepLength, double stepLengthReduction,
            int requiredNQualityFunctionImprovements, Random random)
    {
        super(attraction, repulsion, edgeWeightIncrement);

        this.maxNIterations = maxNIterations;
        this.initialStepLength = initialStepLength;
        this.minStepLength = minStepLength;
        this.stepLengthReduction = stepLengthReduction;
        this.requiredNQualityFunctionImprovements = requiredNQualityFunctionImprovements;
        this.random = random;
    }

    public GradientDescentVOSLayoutAlgorithm clone()
    {
        GradientDescentVOSLayoutAlgorithm gradientDescentVOSLayoutAlgorithm;

        gradientDescentVOSLayoutAlgorithm = (GradientDescentVOSLayoutAlgorithm)super.clone();

        return gradientDescentVOSLayoutAlgorithm;
    }

    public Layout findLayout(Network network)
    {
        Layout layout;

        layout = new Layout(network.getNNodes());
        layout.initRandomCoordinates(random);
        improveLayout(network, layout);
        return layout;
    }

    public void improveLayout(Network network, Layout layout)
    {
        boolean[] nodeVisited;
        double a, b, distance, distance1, distance2, gradient1, gradient2, gradientLength, qualityFunction,
                qualityFunctionOld, squaredDistance, stepLength;
        int i, j, k, l, nQualityFunctionImprovements;
        int[] nodePermutation;

        nodePermutation = cwts.util.Arrays.generateRandomPermutation(network.nNodes, random);

        stepLength = initialStepLength;
        qualityFunction = Double.POSITIVE_INFINITY;
        nQualityFunctionImprovements = 0;
        nodeVisited = new boolean[network.nNodes];
        i = 0;
        while ((i < maxNIterations) && (stepLength >= minStepLength))
        {
            qualityFunctionOld = qualityFunction;
            qualityFunction = 0;
            Arrays.fill(nodeVisited, false);
            for (j = 0; j < network.nNodes; j++)
            {
                k = nodePermutation[j];

                gradient1 = 0;
                gradient2 = 0;

                for (l = network.firstNeighborIndices[k]; l < network.firstNeighborIndices[k + 1]; l++)
                {
                    distance1 = layout.coordinates[0][k] - layout.coordinates[0][network.neighbors[l]];
                    distance2 = layout.coordinates[1][k] - layout.coordinates[1][network.neighbors[l]];
                    squaredDistance = distance1 * distance1 + distance2 * distance2;

                    distance = Math.sqrt(squaredDistance);
                    a = FastMath.fastPow(distance, attraction);

                    if (squaredDistance > 0)
                    {
                        b = network.edgeWeights[l] * a / squaredDistance;
                        gradient1 += b * distance1;
                        gradient2 += b * distance2;
                    }

                    if (!nodeVisited[network.neighbors[l]])
                        if (attraction != 0)
                            qualityFunction += network.edgeWeights[l] * a / attraction;
                        else
                            qualityFunction += network.edgeWeights[l] * Math.log(distance);
                }

                for (l = 0; l < network.nNodes; l++)
                    if (l != k)
                    {
                        distance1 = layout.coordinates[0][k] - layout.coordinates[0][l];
                        distance2 = layout.coordinates[1][k] - layout.coordinates[1][l];
                        squaredDistance = distance1 * distance1 + distance2 * distance2;
                        distance = Math.sqrt(squaredDistance);
                        a = FastMath.fastPow(distance, repulsion);

                        if (squaredDistance > 0)
                        {
                            b = network.nodeWeights[k] * network.nodeWeights[l] * a / squaredDistance;
                            gradient1 -= b * distance1;
                            gradient2 -= b * distance2;
                        }

                        if (!nodeVisited[l])
                            if (repulsion != 0)
                                qualityFunction -= network.nodeWeights[k] * network.nodeWeights[l] * a / repulsion;
                            else
                                qualityFunction -= network.nodeWeights[k] * network.nodeWeights[l] * Math.log(distance);
                    }

                if (edgeWeightIncrement > 0)
                    for (l = 0; l < network.nNodes; l++)
                        if (l != k)
                        {
                            distance1 = layout.coordinates[0][k] - layout.coordinates[0][l];
                            distance2 = layout.coordinates[1][k] - layout.coordinates[1][l];
                            squaredDistance = distance1 * distance1 + distance2 * distance2;
                            distance = Math.sqrt(squaredDistance);
                            a = FastMath.fastPow(distance, attraction);

                            if (squaredDistance > 0)
                            {
                                b = edgeWeightIncrement * a / squaredDistance;
                                gradient1 += b * distance1;
                                gradient2 += b * distance2;
                            }

                            if (!nodeVisited[l])
                                if (attraction != 0)
                                    qualityFunction += edgeWeightIncrement * a / attraction;
                                else
                                    qualityFunction += edgeWeightIncrement * Math.log(distance);
                        }

                gradientLength = Math.sqrt(gradient1 * gradient1 + gradient2 * gradient2);
                layout.coordinates[0][k] -= stepLength * gradient1 / gradientLength;
                layout.coordinates[1][k] -= stepLength * gradient2 / gradientLength;

                nodeVisited[k] = true;
            }

            if (qualityFunction < qualityFunctionOld)
            {
                nQualityFunctionImprovements++;
                if (nQualityFunctionImprovements >= requiredNQualityFunctionImprovements)
                {
                    stepLength /= stepLengthReduction;
                    nQualityFunctionImprovements = 0;
                }
            }
            else
            {
                stepLength *= stepLengthReduction;
                nQualityFunctionImprovements = 0;
            }

            i++;
        }
    }
}
