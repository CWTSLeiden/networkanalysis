package cwts.networkanalysis;

import java.util.Random;

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

    }
}
