package cwts.networkanalysis.run;

import cwts.networkanalysis.Clustering;
import cwts.networkanalysis.CPMClusteringAlgorithm;
import cwts.networkanalysis.IterativeCPMClusteringAlgorithm;
import cwts.networkanalysis.LeidenAlgorithm;
import cwts.networkanalysis.LouvainAlgorithm;
import cwts.networkanalysis.Network;
import java.util.Random;

/**
 * Command line tool for running the Leiden and Louvain algorithms for network
 * clustering.
 *
 * <p>
 * All methods in this class are static.
 * </p>
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 * @author Vincent Traag
 */
public final class RunNetworkClustering
{
    /**
     * Quality function IDs.  
     */
    public static final int CPM = 0;
    public static final int MODULARITY = 1;

    /**
     * Normalization method IDs.
     */
    public static final int NO_NORMALIZATION = 0;
    public static final int ASSOCIATION_STRENGTH = 1;
    public static final int FRACTIONALIZATION = 2;

    /**
     * Clustering algorithm IDs.
     */
    public static final int LEIDEN = 0;
    public static final int LOUVAIN = 1;

    /**
     * Quality function names.
     */
    public static final String[] QUALITY_FUNCTION_NAMES = { "CPM", "Modularity" };

    /**
     * Normalization method names.
     */
    public static final String[] NORMALIZATION_NAMES = { "none", "AssociationStrength", "Fractionalization" };

    /**
     * Clustering algorithm names.
     */
    public static final String[] ALGORITHM_NAMES = { "Leiden", "Louvain" };

    /**
     * Default quality function.
     */
    public static final int DEFAULT_QUALITY_FUNCTION = CPM;

    /**
     * Default normalization method.
     */
    public static final int DEFAULT_NORMALIZATION = NO_NORMALIZATION;

    /**
     * Default clustering algorithm.
     */
    public static final int DEFAULT_ALGORITHM = LEIDEN;

    /**
     * Default resolution parameter.
     */
    public static final double DEFAULT_RESOLUTION = CPMClusteringAlgorithm.DEFAULT_RESOLUTION;

    /**
     * Default minimum cluster size.
     */
    public static final int DEFAULT_MIN_CLUSTER_SIZE = 1;

    /**
     * Default number of random starts.
     */
    public static final int DEFAULT_N_RANDOM_STARTS = 1;

    /**
     * Default number of iterations.
     */
    public static final int DEFAULT_N_ITERATIONS = 10;

    /**
     * Default randomness parameter.
     */
    public static final double DEFAULT_RANDOMNESS = LeidenAlgorithm.DEFAULT_RANDOMNESS;

    /**
     * Description text.
     */
    public static final String DESCRIPTION
        = "RunNetworkClustering version 1.1.0\n"
          + "By Vincent Traag, Ludo Waltman, and Nees Jan van Eck\n"
          + "Centre for Science and Technology Studies (CWTS), Leiden University\n";

    /**
     * Usage text.
     */
    public static final String USAGE
        = "Usage: RunNetworkClustering [options] <filename>\n"
          + "\n"
          + "Identify clusters (also known as communities) in a network using either the\n"
          + "Leiden or the Louvain algorithm.\n"
          + "\n"
          + "The file in <filename> is expected to contain a tab-separated edge list\n"
          + "(without a header line). Nodes are represented by zero-index integer numbers.\n"
          + "Only undirected networks are supported. Each edge should be included only once\n"
          + "in the file.\n"
          + "\n"
          + "Options:\n"
          + "-q --quality-function {" + QUALITY_FUNCTION_NAMES[CPM] + "|" + QUALITY_FUNCTION_NAMES[MODULARITY] + "} (default: " + QUALITY_FUNCTION_NAMES[DEFAULT_QUALITY_FUNCTION] + ")\n"
          + "    Quality function to be optimized. Either the CPM (constant Potts model) or\n"
          + "    the modularity quality function can be used.\n"
          + "-n --normalization {" + NORMALIZATION_NAMES[NO_NORMALIZATION] + "|" + NORMALIZATION_NAMES[ASSOCIATION_STRENGTH] + "|" + NORMALIZATION_NAMES[FRACTIONALIZATION] + " (Default: " + NORMALIZATION_NAMES[DEFAULT_NORMALIZATION] + ")\n"
          + "    Method for normalizing edge weights in the CPM quality function.\n"
          + "-r --resolution <resolution> (default: " + DEFAULT_RESOLUTION + ")\n"
          + "    Resolution parameter of the quality function.\n"
          + "-m --min-cluster-size <min. cluster size> (default: " + DEFAULT_MIN_CLUSTER_SIZE + ")\n"
          + "    Minimum number of nodes per cluster.\n"
          + "-a --algorithm {" + ALGORITHM_NAMES[LEIDEN] + "|" + ALGORITHM_NAMES[LOUVAIN] + "} (default: " + ALGORITHM_NAMES[DEFAULT_ALGORITHM] + ")\n"
          + "    Algorithm for optimizing the quality function. Either the Leiden or the\n"
          + "    Louvain algorithm can be used.\n"
          + "-s --random-starts <random starts> (default: " + DEFAULT_N_RANDOM_STARTS + ")\n"
          + "    Number of random starts of the algorithm.\n"
          + "-i --iterations <iterations> (default: " + DEFAULT_N_ITERATIONS + ")\n"
          + "    Number of iterations of the algorithm.\n"
          + "--randomness <randomness> (default: " + DEFAULT_RANDOMNESS + ")\n"
          + "    Randomness parameter of the Leiden algorithm.\n"
          + "--seed <seed> (default: random)\n"
          + "    Seed of the random number generator.\n"
          + "-w --weighted-edges\n"
          + "    Indicates that the edge list file has a third column containing edge\n"
          + "    weights.\n"
          + "--sorted-edge-list\n"
          + "    Indicates that the edge list file is sorted. The file should be sorted based\n"
          + "    on the nodes in the first column, followed by the nodes in the second\n"
          + "    column. Each edge should be included in both directions in the file.\n"
          + "--input-clustering <filename> (default: singleton clustering)\n"
          + "    Read the initial clustering from the specified file. The file is expected to\n"
          + "    contain two tab-separated columns (without a header line), first a column of\n"
          + "    nodes and then a column of clusters. Nodes and clusters are both represented\n"
          + "    by zero-index integer numbers. If no file is specified, a singleton\n"
          + "    clustering (in which each node has its own cluster) is used as the initial\n"
          + "    clustering.\n"
          + "-o --output-clustering <filename> (default: standard output)\n"
          + "    Write the final clustering to the specified file. If no file is specified,\n"
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

        boolean useModularity = (DEFAULT_QUALITY_FUNCTION == MODULARITY);
        int normalization = DEFAULT_NORMALIZATION;
        double resolution = DEFAULT_RESOLUTION;
        int minClusterSize = DEFAULT_MIN_CLUSTER_SIZE;
        boolean useLouvain = (DEFAULT_ALGORITHM == LOUVAIN);
        int nRandomStarts = DEFAULT_N_RANDOM_STARTS;
        int nIterations = DEFAULT_N_ITERATIONS;
        double randomness = DEFAULT_RANDOMNESS;

        long seed = 0;
        boolean useSeed = false;
        boolean weightedEdges = false;
        boolean sortedEdgeList = false;
        String initialClusteringFilename = null;
        String finalClusteringFilename = null;
        String edgeListFilename = null;

        int argIndex = 0;
        while (argIndex < args.length - 1)
        {
            String arg = args[argIndex];
            try
            {
                if (arg.equals("-q") || arg.equals("--quality-function"))
                {
                    if (((argIndex + 1) >= args.length) || (!args[argIndex + 1].equals(QUALITY_FUNCTION_NAMES[CPM]) && !args[argIndex + 1].equals(QUALITY_FUNCTION_NAMES[MODULARITY])))
                        throw new IllegalArgumentException("Value must be '" + QUALITY_FUNCTION_NAMES[CPM] + "' or '" + QUALITY_FUNCTION_NAMES[MODULARITY] + "'.");
                    useModularity = args[argIndex + 1].equals(QUALITY_FUNCTION_NAMES[MODULARITY]);
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
                else if (arg.equals("-r") || arg.equals("--resolution"))
                {
                    try
                    {
                        if ((argIndex + 1) >= args.length)
                            throw new NumberFormatException();
                        resolution = Double.parseDouble(args[argIndex + 1]);
                        if (resolution < 0)
                            throw new NumberFormatException();
                    }
                    catch (NumberFormatException e)
                    {
                        throw new IllegalArgumentException("Value must be a non-negative number.");
                    }
                    argIndex += 2;
                }
                else if (arg.equals("-m") || arg.equals("--min-cluster-size"))
                {
                    try
                    {
                        if ((argIndex + 1) >= args.length)
                            throw new NumberFormatException();
                        minClusterSize = Integer.parseInt(args[argIndex + 1]);
                        if (minClusterSize <= 0)
                            throw new NumberFormatException();
                    }
                    catch (NumberFormatException e)
                    {
                        throw new IllegalArgumentException("Value must be a positive integer number.");
                    }
                    argIndex += 2;
                }
                else if (arg.equals("-a") || arg.equals("--algorithm"))
                {
                    if (((argIndex + 1) >= args.length) || (!args[argIndex + 1].equals(ALGORITHM_NAMES[LEIDEN]) && !args[argIndex + 1].equals(ALGORITHM_NAMES[LOUVAIN])))
                        throw new IllegalArgumentException("Value must be '" + ALGORITHM_NAMES[LEIDEN] + "' or '" + ALGORITHM_NAMES[LOUVAIN] + "'.");
                    useLouvain = args[argIndex + 1].equals(ALGORITHM_NAMES[LOUVAIN]);
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
                else if (arg.equals("-i") || arg.equals("--iterations"))
                {
                    try
                    {
                        if ((argIndex + 1) >= args.length)
                            throw new NumberFormatException();
                        nIterations = Integer.parseInt(args[argIndex + 1]);
                        if (nIterations <= 0)
                            throw new NumberFormatException();
                    }
                    catch (NumberFormatException e)
                    {
                        throw new IllegalArgumentException("Value must be a positive integer number.");
                    }
                    argIndex += 2;
                }
                else if (arg.equals("--randomness"))
                {
                    try
                    {
                        if ((argIndex + 1) >= args.length)
                            throw new NumberFormatException();
                        randomness = Double.parseDouble(args[argIndex + 1]);
                        if (randomness <= 0)
                            throw new NumberFormatException();
                    }
                    catch (NumberFormatException e)
                    {
                        throw new IllegalArgumentException("Value must be a positive number.");
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
                else if (arg.equals("--input-clustering"))
                {
                    if ((argIndex + 1) >= args.length)
                        throw new IllegalArgumentException("Missing value.");
                    initialClusteringFilename = args[argIndex + 1];
                    argIndex += 2;
                }
                else if (arg.equals("-o") || arg.equals("--output-clustering"))
                {
                    if ((argIndex + 1) >= args.length)
                        throw new IllegalArgumentException("Missing value.");
                    finalClusteringFilename = args[argIndex + 1];
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
        edgeListFilename = args[argIndex];

        // Read edge list from file.
        System.err.println("Reading " + (sortedEdgeList ? "sorted " : "") + "edge list from '" + edgeListFilename + "'.");
        long startTimeEdgeListFile = System.currentTimeMillis();
        Network network = FileIO.readEdgeList(edgeListFilename, weightedEdges, sortedEdgeList);
        System.err.println("Reading " + (sortedEdgeList ? "sorted " : "") + "edge list took " + (System.currentTimeMillis() - startTimeEdgeListFile) / 1000 + "s.");
        System.err.println("Network consists of " + network.getNNodes() + " nodes and " + network.getNEdges() + " edges" + (weightedEdges ? " with a total edge weight of " + network.getTotalEdgeWeight() : "") + ".");

        // Read initial clustering from file.
        Clustering initialClustering = null;
        if (initialClusteringFilename != null)
        {
            System.err.println("Reading initial clustering from '" + initialClusteringFilename + "'.");
            initialClustering = FileIO.readClustering(initialClusteringFilename, network.getNNodes());
            System.err.println("Initial clustering consists of " + initialClustering.getNClusters() + " clusters.");
        }
        else
        {
            System.err.println("Using singleton initial clustering.");
            initialClustering = new Clustering(network.getNNodes());
        }

        // Run algorithm for network clustering.
        System.err.println("Running " + (useLouvain ? ALGORITHM_NAMES[LOUVAIN] : ALGORITHM_NAMES[LEIDEN]) + " algorithm.");
        System.err.println("Quality function:             " + (useModularity ? QUALITY_FUNCTION_NAMES[MODULARITY] : QUALITY_FUNCTION_NAMES[CPM]));
        if (!useModularity)
            System.err.println("Normalization method:         " + NORMALIZATION_NAMES[normalization]);
        System.err.println("Resolution parameter:         " + resolution);
        System.err.println("Minimum cluster size:         " + minClusterSize);
        System.err.println("Number of random starts:      " + nRandomStarts);
        System.err.println("Number of iterations:         " + nIterations);
        if (!useLouvain)
            System.err.println("Randomness parameter:         " + randomness);
        System.err.println("Random number generator seed: " + (useSeed ? seed : "random"));

        long startTimeAlgorithm = System.currentTimeMillis();
        if (!useModularity)
        {
            if (normalization == NO_NORMALIZATION)
                network = network.createNetworkWithoutNodeWeights();
            else if (normalization == ASSOCIATION_STRENGTH)
                network = network.createNormalizedNetworkUsingAssociationStrength();
            else if (normalization == FRACTIONALIZATION)
                network = network.createNormalizedNetworkUsingFractionalization();
        }
        double resolution2 = useModularity ? (resolution / (2 * network.getTotalEdgeWeight() + network.getTotalEdgeWeightSelfLinks())) : resolution;
        Random random = useSeed ? new Random(seed) : new Random();
        IterativeCPMClusteringAlgorithm algorithm = useLouvain ? new LouvainAlgorithm(resolution2, nIterations, random) : new LeidenAlgorithm(resolution2, nIterations, randomness, random);
        Clustering finalClustering = null;
        double maxQuality = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < nRandomStarts; i++)
        {
            Clustering clustering = initialClustering.clone();
            algorithm.improveClustering(network, clustering);
            double quality = algorithm.calcQuality(network, clustering);
            if (nRandomStarts > 1)
                System.err.println("Quality function in random start " + (i + 1) + " equals " + quality + ".");
            if (quality > maxQuality)
            {
                finalClustering = clustering;
                maxQuality = quality;
            }
        }
        finalClustering.orderClustersByNNodes();
        System.err.println("Running algorithm took " + (System.currentTimeMillis() - startTimeAlgorithm) / 1000 + "s.");
        if (nRandomStarts > 1)
            System.err.println("Maximum value of quality function in " + nRandomStarts + " random starts equals " + maxQuality + ".");
        else
            System.err.println("Quality function equals " + maxQuality + ".");
        if (minClusterSize > 1)
        {
            System.err.println("Clustering consists of " + finalClustering.getNClusters() + " clusters.");
            System.err.println("Removing clusters consisting of fewer than " + minClusterSize + " nodes.");
            algorithm.removeSmallClustersBasedOnNNodes(network, finalClustering, minClusterSize);
        }
        System.err.println("Final clustering consists of " + finalClustering.getNClusters() + " clusters.");

        // Write final clustering to file (or to standard output).
        System.err.println("Writing final clustering to " + ((finalClusteringFilename == null) ? "standard output." : "'" + finalClusteringFilename + "'."));
        FileIO.writeClustering(finalClusteringFilename, finalClustering);
    }

    private RunNetworkClustering()
    {
    }
}
