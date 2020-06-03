package cwts.networkanalysis.run;

import cwts.networkanalysis.GradientDescentVOSLayoutAlgorithm;
import cwts.networkanalysis.Layout;
import cwts.networkanalysis.Network;
import java.util.Random;

/**
 * Command line tool for running the gradient descent VOS layout algorithm for
 * network layout.
 *
 * <p>
 * All methods in this class are static.
 * </p>
 *
 * @author Nees Jan van Eck
 * @author Ludo Waltman
 */
public class RunNetworkLayout
{
    /**
     * Quality function IDs.  
     */
    public static final int VOS = 0;
    public static final int LINLOG = 1;

    /**
     * Normalization method IDs.
     */
    public static final int NO_NORMALIZATION = 0;
    public static final int ASSOCIATION_STRENGTH = 1;
    public static final int FRACTIONALIZATION = 2;

    /**
     * Quality function names.
     */
    public static final String[] QUALITY_FUNCTION_NAMES = { "VOS", "LinLog" };

    /**
     * Normalization method names.
     */
    public static final String[] NORMALIZATION_NAMES = { "none", "AssociationStrength", "Fractionalization" };

    /**
     * Edge weight increment unconnected nodes.
     */
    public static final double EDGE_WEIGHT_INCREMENT_UNCONNECTED_NODES = 0.01;

    /**
     * Default quality function.
     */
    public static final int DEFAULT_QUALITY_FUNCTION = VOS;

    /**
     * Default normalization method.
     */
    public static final int DEFAULT_NORMALIZATION = NO_NORMALIZATION;

    /**
     * Default attraction parameter.
     */
    public static final int DEFAULT_ATTRACTION = GradientDescentVOSLayoutAlgorithm.DEFAULT_ATTRACTION;

    /**
     * Default repulsion parameter.
     */
    public static final int DEFAULT_REPULSION = GradientDescentVOSLayoutAlgorithm.DEFAULT_REPULSION;

    /**
     * Default number of random starts.
     */
    public static final int DEFAULT_N_RANDOM_STARTS = 1;

    /**
     * Default maximum number of iterations.
     */
    public static final int DEFAULT_MAX_N_ITERATIONS = GradientDescentVOSLayoutAlgorithm.DEFAULT_MAX_N_ITERATIONS;

    /**
     * Default initial step size.
     */
    public static final double DEFAULT_INITIAL_STEP_SIZE = GradientDescentVOSLayoutAlgorithm.DEFAULT_INITIAL_STEP_SIZE;

    /**
     * Default minimum step size.
     */
    public static final double DEFAULT_MIN_STEP_SIZE = GradientDescentVOSLayoutAlgorithm.DEFAULT_MIN_STEP_SIZE;

    /**
     * Default step size reduction.
     */
    public static final double DEFAULT_STEP_SIZE_REDUCTION = GradientDescentVOSLayoutAlgorithm.DEFAULT_STEP_SIZE_REDUCTION;

    /**
     * Default required number of quality value improvements.
     */
    public static final int DEFAULT_REQUIRED_N_QUALITY_VALUE_IMPROVEMENTS = GradientDescentVOSLayoutAlgorithm.DEFAULT_REQUIRED_N_QUALITY_VALUE_IMPROVEMENTS;

    /**
     * Description text.
     */
    public static final String DESCRIPTION
        = "RunNetworkLayout version 1.1.0\n"
          + "By Nees Jan van Eck and Ludo Waltman\n"
          + "Centre for Science and Technology Studies (CWTS), Leiden University\n";

    /**
     * Usage text.
     */
    public static final String USAGE
        = "Usage: RunNetworkLayout [options] <filename>\n"
          + "\n"
          + "Determine a layout for a network using the gradient descent VOS layout\n"
          + "algorithm.\n"
          + "\n"
          + "The file in <filename> is expected to contain a tab-separated edge list\n"
          + "(without a header line). Nodes are represented by zero-index integer numbers.\n"
          + "Only undirected networks are supported. Each edge should be included only once\n"
          + "in the file.\n"
          + "\n"
          + "Options:\n"
          + "-q --quality-function {" + QUALITY_FUNCTION_NAMES[VOS] + "|" + QUALITY_FUNCTION_NAMES[LINLOG] + "} (default: " + QUALITY_FUNCTION_NAMES[DEFAULT_QUALITY_FUNCTION] + ")\n"
          + "    Quality function to be optimized. Either the VOS (visualization of\n"
          + "    similarities) or the LinLog quality function can be used.\n"
          + "-n --normalization {" + NORMALIZATION_NAMES[NO_NORMALIZATION] + "|" + NORMALIZATION_NAMES[ASSOCIATION_STRENGTH] + "|" + NORMALIZATION_NAMES[FRACTIONALIZATION] + " (Default: " + NORMALIZATION_NAMES[NO_NORMALIZATION] + ")\n"
          + "    Method for normalizing edge weights in the VOS quality function.\n"
          + "-a --attraction <attraction> (Default: " + DEFAULT_ATTRACTION + ")\n"
          + "    Attraction parameter of the VOS quality function.\n"
          + "-r --repulsion <repulsion> (Default: " + DEFAULT_REPULSION + ")\n"
          + "    Repulsion parameter of the VOS quality function.\n"
          + "-s --random-starts <random starts> (default: " + DEFAULT_N_RANDOM_STARTS + ")\n"
          + "    Number of random starts of the gradient descent algorithm.\n"
          + "-i --max-iterations <max. iterations> (default: " + DEFAULT_MAX_N_ITERATIONS + ")\n"
          + "    Maximum number of iterations of the gradient descent algorithm.\n"
          + "--initial-step-size <initial step size> (default: " + DEFAULT_INITIAL_STEP_SIZE + ")\n"
          + "    Initial step size of the gradient descent algorithm.\n"
          + "--min-step-size <min. step size> (default: " + DEFAULT_MIN_STEP_SIZE + ")\n"
          + "    Minimum step size of the gradient descent algorithm.\n"
          + "--step-size-reduction <step size reduction> (default: " + DEFAULT_STEP_SIZE_REDUCTION + ")\n"
          + "    Step size reduction of the gradient descent algorithm.\n"
          + "--required-quality-value-improvements <required quality value improvements>\n"
          + "        (default: " + DEFAULT_REQUIRED_N_QUALITY_VALUE_IMPROVEMENTS + ")\n"
          + "    Required number of quality value improvements of the gradient descent\n"
          + "    algorithm.\n"
          + "--seed <seed> (default: random)\n"
          + "    Seed of the random number generator.\n"
          + "-w --weighted-edges\n"
          + "    Indicates that the edge list file has a third column containing edge\n"
          + "    weights.\n"
          + "--sorted-edge-list\n"
          + "    Indicates that the edge list file is sorted. The file should be sorted based\n"
          + "    on the nodes in the first column, followed by the nodes in the second\n"
          + "    column. Each edge should be included in both directions in the file.\n"
          + "--input-layout <filename> (default: random layout)\n"
          + "    Read the initial layout from the specified file. The file is expected to\n"
          + "    contain three tab-separated columns (without a header line), first a column\n"
          + "    of nodes, then a column of x coordinates, and finally a column of\n"
          + "    y coordinates. Nodes are represented by zero-index integer numbers. If no\n"
          + "    file is specified, a random layout (in which each node is positioned at\n"
          + "    random coordinates) is used as the initial layout.\n"
          + "-o --output-layout <filename> (default: standard output)\n"
          + "    Write the final layout to the specified file. If no file is specified,\n"
          + "    the standard output is used.\n";

    /**
     * This method is called when the tool is started.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args)
    {
        System.err.println(DESCRIPTION);

        // Process command line arguments.
        if (args.length == 0)
        {
            System.err.print(USAGE);
            System.exit(-1);
        }

        boolean useLinLog = (DEFAULT_QUALITY_FUNCTION == LINLOG);
        int normalization = DEFAULT_NORMALIZATION;
        int attraction = DEFAULT_ATTRACTION;
        int repulsion = DEFAULT_REPULSION;
        int nRandomStarts = DEFAULT_N_RANDOM_STARTS;
        int maxNIterations = DEFAULT_MAX_N_ITERATIONS;
        double initialStepSize = DEFAULT_INITIAL_STEP_SIZE;
        double minStepSize = DEFAULT_MIN_STEP_SIZE;
        double stepSizeReduction = DEFAULT_STEP_SIZE_REDUCTION;
        int requiredNQualityValueImprovements = DEFAULT_REQUIRED_N_QUALITY_VALUE_IMPROVEMENTS;

        long seed = 0;
        boolean useSeed = false;
        boolean weightedEdges = false;
        boolean sortedEdgeList = false;
        String initialLayoutFilename = null;
        String finalLayoutFilename = null;
        String edgeListFilename = null;

        int argIndex = 0;
        while (argIndex < args.length - 1)
        {
            String arg = args[argIndex];
            try
            {
                if (arg.equals("-q") || arg.equals("--quality-function"))
                {
                    if (((argIndex + 1) >= args.length) || (!args[argIndex + 1].equals(QUALITY_FUNCTION_NAMES[VOS]) && !args[argIndex + 1].equals(QUALITY_FUNCTION_NAMES[LINLOG])))
                        throw new IllegalArgumentException("Value must be '" + QUALITY_FUNCTION_NAMES[VOS] + "' or '" + QUALITY_FUNCTION_NAMES[LINLOG] + "'.");
                    useLinLog = args[argIndex + 1].equals(QUALITY_FUNCTION_NAMES[LINLOG]);
                    argIndex += 2;
                }
                else if (arg.equals("-n") || arg.equals("--normalization"))
                {
                    if (((argIndex + 1) >= args.length) || (!args[argIndex + 1].equals(NORMALIZATION_NAMES[NO_NORMALIZATION]) && !args[argIndex + 1].equals(NORMALIZATION_NAMES[ASSOCIATION_STRENGTH]) && !args[argIndex + 1].equals(NORMALIZATION_NAMES[FRACTIONALIZATION])))
                        throw new IllegalArgumentException("Value must be '" + NORMALIZATION_NAMES[NO_NORMALIZATION] + "', '" + NORMALIZATION_NAMES[ASSOCIATION_STRENGTH] + "', or '" + NORMALIZATION_NAMES[FRACTIONALIZATION] + "'.");
                    if (args[argIndex + 1].equals(NORMALIZATION_NAMES[NO_NORMALIZATION]))
                        normalization = NO_NORMALIZATION;
                    else if (args[argIndex + 1].equals(NORMALIZATION_NAMES[ASSOCIATION_STRENGTH]))
                        normalization = ASSOCIATION_STRENGTH;
                    else if (args[argIndex + 1].equals(NORMALIZATION_NAMES[FRACTIONALIZATION]))
                        normalization = FRACTIONALIZATION;
                    argIndex += 2;
                }
                else if (arg.equals("-a") || arg.equals("--attraction"))
                {
                    try
                    {
                        if ((argIndex + 1) >= args.length)
                            throw new NumberFormatException();
                        attraction = Integer.parseInt(args[argIndex + 1]);
                    }
                    catch (NumberFormatException e)
                    {
                        throw new IllegalArgumentException("Value must be an integer number.");
                    }
                    argIndex += 2;
                }
                else if (arg.equals("-r") || arg.equals("--repulsion"))
                {
                    try
                    {
                        if ((argIndex + 1) >= args.length)
                            throw new NumberFormatException();
                        repulsion = Integer.parseInt(args[argIndex + 1]);
                    }
                    catch (NumberFormatException e)
                    {
                        throw new IllegalArgumentException("Value must be an integer number.");
                    }
                    argIndex += 2;
                }
                else if (arg.equals("-s") || arg.equals("--random-starts"))
                {
                    try
                    {
                        if ((argIndex + 1) >= args.length)
                            throw new NumberFormatException();
                        nRandomStarts = Integer.parseInt(args[argIndex + 1]);
                        if (nRandomStarts <= 0)
                            throw new NumberFormatException();
                    }
                    catch (NumberFormatException e)
                    {
                        throw new IllegalArgumentException("Value must be a positive integer number.");
                    }
                    argIndex += 2;
                }
                else if (arg.equals("-i") || arg.equals("--max-iterations"))
                {
                    try
                    {
                        if ((argIndex + 1) >= args.length)
                            throw new NumberFormatException();
                        maxNIterations = Integer.parseInt(args[argIndex + 1]);
                        if (maxNIterations <= 0)
                            throw new NumberFormatException();
                    }
                    catch (NumberFormatException e)
                    {
                        throw new IllegalArgumentException("Value must be a positive integer number.");
                    }
                    argIndex += 2;
                }
                else if (arg.equals("--initial-step-size"))
                {
                    try
                    {
                        if ((argIndex + 1) >= args.length)
                            throw new NumberFormatException();
                        initialStepSize = Double.parseDouble(args[argIndex + 1]);
                        if (initialStepSize <= 0)
                            throw new NumberFormatException();
                    }
                    catch (NumberFormatException e)
                    {
                        throw new IllegalArgumentException("Value must be a positive number.");
                    }
                    argIndex += 2;
                }
                else if (arg.equals("--min-step-size"))
                {
                    try
                    {
                        if ((argIndex + 1) >= args.length)
                            throw new NumberFormatException();
                        minStepSize = Double.parseDouble(args[argIndex + 1]);
                        if (minStepSize <= 0)
                            throw new NumberFormatException();
                    }
                    catch (NumberFormatException e)
                    {
                        throw new IllegalArgumentException("Value must be a positive number.");
                    }
                    argIndex += 2;
                }                
                else if (arg.equals("--step-size-reduction"))
                {
                    try
                    {
                        if ((argIndex + 1) >= args.length)
                            throw new NumberFormatException();
                        stepSizeReduction = Double.parseDouble(args[argIndex + 1]);
                        if (stepSizeReduction <= 0)
                            throw new NumberFormatException();
                    }
                    catch (NumberFormatException e)
                    {
                        throw new IllegalArgumentException("Value must be a positive number.");
                    }
                    argIndex += 2;
                }                  
                else if (arg.equals("--required-quality-value-improvements"))
                {
                    try
                    {
                        if ((argIndex + 1) >= args.length)
                            throw new NumberFormatException();
                        requiredNQualityValueImprovements = Integer.parseInt(args[argIndex + 1]);
                        if (requiredNQualityValueImprovements <= 0)
                            throw new NumberFormatException();
                    }
                    catch (NumberFormatException e)
                    {
                        throw new IllegalArgumentException("Value must be a positive integer number.");
                    }
                    argIndex += 2;
                }
                else if (arg.equals("--seed"))
                {
                    try
                    {
                        if ((argIndex + 1) >= args.length)
                            throw new NumberFormatException();
                        seed = Long.parseLong(args[argIndex + 1]);
                    }
                    catch (NumberFormatException e)
                    {
                        throw new IllegalArgumentException("Value must be an integer number.");
                    }
                    useSeed = true;
                    argIndex += 2;
                }
                else if (arg.equals("-w") || arg.equals("--weighted-edges"))
                {
                    weightedEdges = true;
                    argIndex++;
                }
                else if (arg.equals("--sorted-edge-list"))
                {
                    sortedEdgeList = true;
                    argIndex++;
                }
                else if (arg.equals("--input-layout"))
                {
                    if ((argIndex + 1) >= args.length)
                        throw new IllegalArgumentException("Missing value.");
                    initialLayoutFilename = args[argIndex + 1];
                    argIndex += 2;
                }
                else if (arg.equals("-o") || arg.equals("--output-layout"))
                {
                    if ((argIndex + 1) >= args.length)
                        throw new IllegalArgumentException("Missing value.");
                    finalLayoutFilename = args[argIndex + 1];
                    argIndex += 2;
                }
                else
                    throw new IllegalArgumentException("Invalid command line argument.");
            }
            catch (IllegalArgumentException e)
            {
                System.err.print("Error while processing command line argument " + arg + ": " + e.getMessage() + "\n\n" + USAGE);
                System.exit(-1);
            }
        }
        if (argIndex >= args.length)
        {
            System.err.print("Error while processing command line arguments: Incorrect number of command line arguments.\n\n" + USAGE);
            System.exit(-1);
        }
        if (attraction <= repulsion)
        {
            System.err.print("Error while processing command line arguments: Attraction value must be greater than repulsion value.\n\n" + USAGE);
            System.exit(-1);
        }
        edgeListFilename = args[argIndex];

        // Read edge list from file.
        System.err.println("Reading " + (sortedEdgeList ? "sorted " : "") + "edge list from '" + edgeListFilename + "'.");
        long startTimeEdgeListFile = System.currentTimeMillis();
        Network network = FileIO.readEdgeList(edgeListFilename, weightedEdges, sortedEdgeList);
        System.err.println("Reading " + (sortedEdgeList ? "sorted " : "") + "edge list took " + (System.currentTimeMillis() - startTimeEdgeListFile) / 1000 + "s.");
        System.err.println("Network consists of " + network.getNNodes() + " nodes and " + network.getNEdges() + " edges" + (weightedEdges ? " with a total edge weight of " + network.getTotalEdgeWeight() : "") + ".");

        // Read initial layout from file.
        Layout initialLayout = null;
        if (initialLayoutFilename != null)
        {
            System.err.println("Reading initial layout from '" + initialLayoutFilename + "'.");
            initialLayout = FileIO.readLayout(initialLayoutFilename, network.getNNodes());
        }

        // Run algorithm for network layout.
        System.err.println("Running gradient descent VOS layout algorithm.");
        System.err.println("Quality function:                              " + (useLinLog ? QUALITY_FUNCTION_NAMES[LINLOG] : QUALITY_FUNCTION_NAMES[VOS]));
        System.err.println("Normalization method:                          " + NORMALIZATION_NAMES[normalization]);
        System.err.println("Attraction parameter:                          " + attraction);
        System.err.println("Repulsion parameter:                           " + repulsion);
        System.err.println("Number of random starts:                       " + nRandomStarts);
        System.err.println("Maximum number of iterations:                  " + maxNIterations);
        System.err.println("Initial step size:                             " + initialStepSize);
        System.err.println("Minimum step size:                             " + minStepSize);
        System.err.println("Step size reduction:                           " + stepSizeReduction);
        System.err.println("Required number of quality value improvements: " + requiredNQualityValueImprovements);
        System.err.println("Random number generator seed:                  " + (useSeed ? seed : "random"));

        long startTimeAlgorithm = System.currentTimeMillis();
        if (!useLinLog)
        {
            if (normalization == NO_NORMALIZATION)
                network = network.createNetworkWithoutNodeWeights();
            else if (normalization == ASSOCIATION_STRENGTH)
                network = network.createNormalizedNetworkUsingAssociationStrength();
            else if (normalization == FRACTIONALIZATION)
                network = network.createNormalizedNetworkUsingFractionalization();
        }
        double edgeWeightIncrement = (network.identifyComponents().getNClusters() > 1) ? EDGE_WEIGHT_INCREMENT_UNCONNECTED_NODES : 0;
        Random random = useSeed ? new Random(seed) : new Random();
        GradientDescentVOSLayoutAlgorithm algorithm = new GradientDescentVOSLayoutAlgorithm(attraction, repulsion, edgeWeightIncrement, random);
        Layout finalLayout = null;
        double minQuality = Double.POSITIVE_INFINITY;
        for (int i = 0; i < nRandomStarts; i++)
        {
            Layout layout = (initialLayout != null) ? initialLayout.clone() : new Layout(network.getNNodes(), random);
            algorithm.improveLayout(network, layout);
            double quality = algorithm.calcQuality(network, layout);
            if (nRandomStarts > 1)
                System.err.println("Quality function in random start " + (i + 1) + " equals " + quality + ".");
            if (quality < minQuality)
            {
                finalLayout = layout;
                minQuality = quality;
            }
        }
        finalLayout.standardizeCoordinates(true);
        System.err.println("Running algorithm took " + (System.currentTimeMillis() - startTimeAlgorithm) / 1000 + "s.");
        if (nRandomStarts > 1)
            System.err.println("Minimum value of quality function in " + nRandomStarts + " random starts equals " + minQuality + ".");
        else
            System.err.println("Quality function equals " + minQuality + ".");

        // Write final layout to file (or to standard output).
        System.err.println("Writing final layout to " + ((finalLayoutFilename == null) ? "standard output." : "'" + finalLayoutFilename + "'."));
        FileIO.writeLayout(finalLayoutFilename, finalLayout);
    }

    private RunNetworkLayout()
    {
    }
}
