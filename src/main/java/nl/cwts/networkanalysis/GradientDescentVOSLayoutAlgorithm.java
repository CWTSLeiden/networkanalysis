package nl.cwts.networkanalysis;

import java.util.Arrays;
import java.util.Random;

import nl.cwts.util.FastMath;

/**
 * Gradient descent VOS layout algorithm.
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 */
public class GradientDescentVOSLayoutAlgorithm extends VOSLayoutAlgorithm
{
    /**
     * Default maximum number of iterations.
     */
    public static final int DEFAULT_MAX_N_ITERATIONS = 1000;

    /**
     * Default initial step size.
     */
    public static final double DEFAULT_INITIAL_STEP_SIZE = 1;

    /**
     * Default minimum step size.
     */
    public static final double DEFAULT_MIN_STEP_SIZE = 0.001;

    /**
     * Default step size reduction.
     */
    public static final double DEFAULT_STEP_SIZE_REDUCTION = 0.75;

    /**
     * Default required number of quality value improvements.
     */
    public static final int DEFAULT_REQUIRED_N_QUALITY_VALUE_IMPROVEMENTS = 5;

    /**
     * Maximum number of iterations.
     */
    protected int maxNIterations;

    /**
     * Initial step size.
     */
    protected double initialStepSize;

    /**
     * Minimum step size.
     */
    protected double minStepSize;

    /**
     * Step size reduction.
     */
    protected double stepSizeReduction;

    /**
     * Required number of quality value improvements.
     */
    protected int requiredNQualityValueImprovements;

    /**
     * Random number generator.
     */
    protected Random random;

    /**
     * Constructs a gradient descent VOS layout algorithm.
     */
    public GradientDescentVOSLayoutAlgorithm()
    {
        this(new Random());
    }

    /**
     * Constructs a gradient descent VOS layout algorithm.
     *
     * @param random Random number generator
     */
    public GradientDescentVOSLayoutAlgorithm(Random random)
    {
        this(DEFAULT_ATTRACTION, DEFAULT_REPULSION, DEFAULT_EDGE_WEIGHT_INCREMENT, random);
    }

    /**
     * Constructs a gradient descent VOS layout algorithm for a specified
     * attraction parameter, repulsion parameter, and edge weight increment
     * parameter.
     *
     * @param attraction          Attraction parameter
     * @param repulsion           Repulsion parameter
     * @param edgeWeightIncrement Edge weight increment parameter
     * @param random              Random number generator
     */
    public GradientDescentVOSLayoutAlgorithm(int attraction, int repulsion, double edgeWeightIncrement, Random random)
    {
        this(attraction, repulsion, edgeWeightIncrement, DEFAULT_MAX_N_ITERATIONS, DEFAULT_INITIAL_STEP_SIZE,
                DEFAULT_MIN_STEP_SIZE, DEFAULT_STEP_SIZE_REDUCTION, DEFAULT_REQUIRED_N_QUALITY_VALUE_IMPROVEMENTS,
                random);
    }

    /**
     * Constructs a gradient descent VOS layout algorithm for a specified
     * attraction parameter, repulsion parameter, edge weight increment
     * parameter, maximum number of iterations, initial step size, minimum step
     * size, step size reduction, and required number of quality value
     * improvements.
     *
     * @param attraction                        Attraction parameter
     * @param repulsion                         Repulsion parameter
     * @param edgeWeightIncrement               Edge weight increment parameter
     * @param maxNIterations                    Maximum number of iterations
     * @param initialStepSize                   Initial step size
     * @param minStepSize                       Minimum step size
     * @param stepSizeReduction                 Step size reduction
     * @param requiredNQualityValueImprovements Required number of quality value
     *            improvements
     * @param random                            Random number generator
     */
    public GradientDescentVOSLayoutAlgorithm(int attraction, int repulsion, double edgeWeightIncrement,
            int maxNIterations, double initialStepSize, double minStepSize, double stepSizeReduction,
            int requiredNQualityValueImprovements, Random random)
    {
        super(attraction, repulsion, edgeWeightIncrement);

        this.maxNIterations = maxNIterations;
        this.initialStepSize = initialStepSize;
        this.minStepSize = minStepSize;
        this.stepSizeReduction = stepSizeReduction;
        this.requiredNQualityValueImprovements = requiredNQualityValueImprovements;
        this.random = random;
    }

    /**
     * Clones the algorithm.
     *
     * @return Cloned algorithm
     */
    public GradientDescentVOSLayoutAlgorithm clone()
    {
        GradientDescentVOSLayoutAlgorithm gradientDescentVOSLayoutAlgorithm;

        gradientDescentVOSLayoutAlgorithm = (GradientDescentVOSLayoutAlgorithm)super.clone();

        return gradientDescentVOSLayoutAlgorithm;
    }

    /**
     * Returns the maximum number of iterations.
     *
     * @return Maximum number of iterations
     */
    public int getMaxNIterations()
    {
        return maxNIterations;
    }

    /**
     * Returns the initial step size.
     *
     * @return Initial step size
     */
    public double getInitialStepSize()
    {
        return initialStepSize;
    }

    /**
     * Returns the minimum step size.
     *
     * @return Minimum step size
     */
    public double getMinStepSize()
    {
        return minStepSize;
    }

    /**
     * Returns the step size reduction.
     *
     * @return Step size reduction
     */
    public double getStepSizeReduction()
    {
        return stepSizeReduction;
    }

    /**
     * Returns the required number of quality value improvements.
     *
     * @return Required number of quality value improvements
     */
    public int getRequiredNQualityValueImprovements()
    {
        return requiredNQualityValueImprovements;
    }

    /**
     * Sets the maximum number of iterations.
     * 
     * @param maxNIterations Maximum number of iterations
     */
    public void setMaxNIterations(int maxNIterations)
    {
        this.maxNIterations = maxNIterations;
    }

    /**
     * Sets the initial step size.
     * 
     * @param initialStepSize Initial step size
     */
    public void setInitialStepSize(double initialStepSize)
    {
        this.initialStepSize = initialStepSize;
    }

    /**
     * Sets the minimum step size.
     * 
     * @param minStepSize Minimum step size
     */
    public void setMinStepSize(double minStepSize)
    {
        this.minStepSize = minStepSize;
    }

    /**
     * Sets the step size reduction.
     * 
     * @param stepSizeReduction Step size reduction
     */
    public void setStepSizeReduction(double stepSizeReduction)
    {
        this.stepSizeReduction = stepSizeReduction;
    }

    /**
     * Sets the required number of quality value improvements.
     * 
     * @param requiredNQualityValueImprovements Required number of quality value
     *            improvements
     */
    public void setRequiredNQualityValueImprovements(int requiredNQualityValueImprovements)
    {
        this.requiredNQualityValueImprovements = requiredNQualityValueImprovements;
    }

    /**
     * Finds a layout using the gradient descent VOS layout algorithm.
     *
     * @param network Network
     *
     * @return Layout
     */
    public Layout findLayout(Network network)
    {
        Layout layout;

        layout = new Layout(network.getNNodes(), random);
        improveLayout(network, layout);
        return layout;
    }

    /**
     * Improves a layout using the gradient descent VOS layout algorithm.
     *
     * @param network Network
     * @param layout  Layout
     */
    public void improveLayout(Network network, Layout layout)
    {
        boolean[] visitedNodes;
        double a, b, distance, distance1, distance2, gradient1, gradient2, gradientLength, qualityValue,
                oldQualityValue, squaredDistance, stepSize;
        int i, j, k, l, nQualityValueImprovements;
        long e;
        int[] nodeOrder;

        nodeOrder = nl.cwts.util.Arrays.generateRandomPermutation(network.nNodes, random);

        stepSize = initialStepSize;
        qualityValue = Double.POSITIVE_INFINITY;
        nQualityValueImprovements = 0;
        visitedNodes = new boolean[network.nNodes];
        i = 0;
        while ((i < maxNIterations) && (stepSize >= minStepSize))
        {
            oldQualityValue = qualityValue;
            qualityValue = 0;
            Arrays.fill(visitedNodes, false);
            for (j = 0; j < network.nNodes; j++)
            {
                k = nodeOrder[j];

                gradient1 = 0;
                gradient2 = 0;

                for (e = network.firstNeighborIndices[k]; e < network.firstNeighborIndices[k + 1]; e++)
                {
                    distance1 = layout.coordinates[0][k] - layout.coordinates[0][network.neighbors.get(e)];
                    distance2 = layout.coordinates[1][k] - layout.coordinates[1][network.neighbors.get(e)];
                    squaredDistance = distance1 * distance1 + distance2 * distance2;

                    distance = Math.sqrt(squaredDistance);
                    a = FastMath.fastPow(distance, attraction);

                    if (squaredDistance > 0)
                    {
                        b = network.edgeWeights.get(e) * a / squaredDistance;
                        gradient1 += b * distance1;
                        gradient2 += b * distance2;
                    }

                    if (!visitedNodes[network.neighbors.get(e)])
                        if (attraction != 0)
                            qualityValue += network.edgeWeights.get(e) * a / attraction;
                        else
                            qualityValue += network.edgeWeights.get(e) * Math.log(distance);
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

                        if (!visitedNodes[l])
                            if (repulsion != 0)
                                qualityValue -= network.nodeWeights[k] * network.nodeWeights[l] * a / repulsion;
                            else
                                qualityValue -= network.nodeWeights[k] * network.nodeWeights[l] * Math.log(distance);

                        if (edgeWeightIncrement > 0)
                        {
                            a = FastMath.fastPow(distance, attraction);

                            if (squaredDistance > 0)
                            {
                                b = edgeWeightIncrement * a / squaredDistance;
                                gradient1 += b * distance1;
                                gradient2 += b * distance2;
                            }

                            if (!visitedNodes[l])
                                if (attraction != 0)
                                    qualityValue += edgeWeightIncrement * a / attraction;
                                else
                                    qualityValue += edgeWeightIncrement * Math.log(distance);
                        }
                    }

                gradientLength = Math.sqrt(gradient1 * gradient1 + gradient2 * gradient2);
                layout.coordinates[0][k] -= stepSize * gradient1 / gradientLength;
                layout.coordinates[1][k] -= stepSize * gradient2 / gradientLength;

                visitedNodes[k] = true;
            }

            if (qualityValue < oldQualityValue)
            {
                nQualityValueImprovements++;
                if (nQualityValueImprovements >= requiredNQualityValueImprovements)
                {
                    stepSize /= stepSizeReduction;
                    nQualityValueImprovements = 0;
                }
            }
            else
            {
                stepSize *= stepSizeReduction;
                nQualityValueImprovements = 0;
            }

            i++;
        }
    }
}
