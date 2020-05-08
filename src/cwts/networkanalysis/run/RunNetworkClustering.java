package cwts.networkanalysis.run;

import cwts.networkanalysis.Clustering;
import cwts.networkanalysis.CPMClusteringAlgorithm;
import cwts.networkanalysis.IterativeCPMClusteringAlgorithm;
import cwts.networkanalysis.LeidenAlgorithm;
import cwts.networkanalysis.LouvainAlgorithm;
import cwts.networkanalysis.Network;
import cwts.util.DynamicDoubleArray;
import cwts.util.DynamicIntArray;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
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
     * Normalization method IDs.
     */
    public static final int NO_NORMALIZATION = 0;
    public static final int ASSOCIATION_STRENGTH = 1;
    public static final int FRACTIONALIZATION = 2;
    public static final int MODULARITY = 3;

    /**
     * Normalization method names.
     */
    public static final String[] NORMALIZATION_NAMES = { "none", "AssociationStrength", "Fractionalization", "Modularity" };

    /**
     * Default normalization method.
     */
    public static final int DEFAULT_NORMALIZATION = NO_NORMALIZATION;

    /**
     * Default resolution parameter.
     */
    public static final double DEFAULT_RESOLUTION = CPMClusteringAlgorithm.DEFAULT_RESOLUTION;

    /**
     * Default clustering algorithm.
     */
    public static final boolean DEFAULT_USE_LOUVAIN = false;

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
        = "RunNetworkClustering version 1.0.0\n"
          + "By Vincent Traag, Ludo Waltman, and Nees Jan van Eck\n"
          + "Centre for Science and Technology Studies (CWTS), Leiden University\n";

    /**
     * Usage text.
     */
    public static final String USAGE
        = "Usage: RunNetworkClustering [options] <filename>\n"
          + "\n"
          + "Identify clusters (also known as communities) in a network, using either the\n"
          + "Leiden or the Louvain algorithm.\n"
          + "\n"
          + "The file in <filename> is expected to contain a tab-separated edge list\n"
          + "(without a header line). Nodes are represented by zero-index integer numbers.\n"
          + "Only undirected networks are supported. Each edge should be included only once\n"
          + "in the file.\n"
          + "\n"
          + "Options:\n"
          + "-n --normalization {" + NORMALIZATION_NAMES[NO_NORMALIZATION] + "|" + NORMALIZATION_NAMES[ASSOCIATION_STRENGTH] + "|" + NORMALIZATION_NAMES[FRACTIONALIZATION] + "|" + NORMALIZATION_NAMES[MODULARITY] + "} (Default: " + NORMALIZATION_NAMES[NO_NORMALIZATION] + ")\n"
          + "    Method for normalizing the edge weights.\n"
          + "-r --resolution <resolution> (default: " + DEFAULT_RESOLUTION + ")\n"
          + "    Resolution parameter of the quality function.\n"
          + "-a --algorithm {Leiden|Louvain} (default: Leiden)\n"
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
     * Column separator for edge list and clustering files.
     */
    public static final String COLUMN_SEPARATOR = "\t";

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

        int normalization = DEFAULT_NORMALIZATION;
        double resolution = DEFAULT_RESOLUTION;
        boolean useLouvain = DEFAULT_USE_LOUVAIN;
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
                if (arg.equals("-n") || arg.equals("--normalization"))
                {
                    if (((argIndex + 1) >= args.length) || (!args[argIndex + 1].equals(NORMALIZATION_NAMES[NO_NORMALIZATION]) && !args[argIndex + 1].equals(NORMALIZATION_NAMES[ASSOCIATION_STRENGTH]) && !args[argIndex + 1].equals(NORMALIZATION_NAMES[FRACTIONALIZATION]) && !args[argIndex + 1].equals(NORMALIZATION_NAMES[MODULARITY])))
                        throw new IllegalArgumentException("Value must be '" + NORMALIZATION_NAMES[NO_NORMALIZATION] + "', '" + NORMALIZATION_NAMES[ASSOCIATION_STRENGTH] + "', '" + NORMALIZATION_NAMES[FRACTIONALIZATION] + "', or '" + NORMALIZATION_NAMES[MODULARITY] + "'.");
                    if (args[argIndex + 1].equals(NORMALIZATION_NAMES[NO_NORMALIZATION]))
                        normalization = NO_NORMALIZATION;
                    else if (args[argIndex + 1].equals(NORMALIZATION_NAMES[ASSOCIATION_STRENGTH]))
                        normalization = ASSOCIATION_STRENGTH;
                    else if (args[argIndex + 1].equals(NORMALIZATION_NAMES[FRACTIONALIZATION]))
                        normalization = FRACTIONALIZATION;
                    else if (args[argIndex + 1].equals(NORMALIZATION_NAMES[MODULARITY]))
                        normalization = MODULARITY;
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
                else if (arg.equals("-a") || arg.equals("--algorithm"))
                {
                    if (((argIndex + 1) >= args.length) || (!args[argIndex + 1].equals("Leiden") && !args[argIndex + 1].equals("Louvain")))
                        throw new IllegalArgumentException("Value must be 'Leiden' or 'Louvain'.");
                    useLouvain = args[argIndex + 1].equals("Louvain");
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
        Network network = readEdgeList(edgeListFilename, weightedEdges, sortedEdgeList);
        System.err.println("Reading " + (sortedEdgeList ? "sorted " : "") + "edge list took " + (System.currentTimeMillis() - startTimeEdgeListFile) / 1000 + "s.");
        System.err.println("Network consists of " + network.getNNodes() + " nodes and " + network.getNEdges() + " edges" + (weightedEdges ? " with a total edge weight of " + network.getTotalEdgeWeight() : "") + ".");

        // Read initial clustering from file.
        Clustering initialClustering = null;
        if (initialClusteringFilename == null)
        {
            System.err.println("Using singleton initial clustering.");
            initialClustering = new Clustering(network.getNNodes());
        }
        else
        {
            System.err.println("Reading initial clustering from '" + initialClusteringFilename + "'.");
            initialClustering = readClustering(initialClusteringFilename, network.getNNodes());
            System.err.println("Initial clustering consists of " + initialClustering.getNClusters() + " clusters.");
        }

        // Run algorithm for network clustering.
        System.err.println("Running " + (useLouvain ? "Louvain" : "Leiden") + " algorithm.");
        System.err.println("Normalization method:         " + NORMALIZATION_NAMES[normalization]);
        System.err.println("Resolution parameter:         " + resolution);
        if ((!weightedEdges) && (normalization != MODULARITY) && (resolution >= 1))
            System.err.println("Warning: When applying the CPM quality function in an unweighted network, the resolution parameter should have a value below 1.");
        System.err.println("Number of random starts:      " + nRandomStarts);
        System.err.println("Number of iterations:         " + nIterations);
        if (!useLouvain)
            System.err.println("Randomness parameter:         " + randomness);
        System.err.println("Random number generator seed: " + (useSeed ? seed : "random"));

        long startTimeAlgorithm = System.currentTimeMillis();
        if (normalization == NO_NORMALIZATION)
            network = network.createNetworkWithoutNodeWeights();
        else if (normalization == ASSOCIATION_STRENGTH)
            network = network.createNormalizedNetworkUsingAssociationStrength();
        else if (normalization == FRACTIONALIZATION)
            network = network.createNormalizedNetworkUsingFractionalization();
        double resolution2 = (normalization == MODULARITY) ? (resolution / (2 * network.getTotalEdgeWeight() + network.getTotalEdgeWeightSelfLinks())) : resolution;
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
        System.err.println("Running algorithm took " + (System.currentTimeMillis() - startTimeAlgorithm) / 1000 + "s.");
        if (nRandomStarts > 1)
            System.err.println("Maximum value of quality function in " + nRandomStarts + " random starts equals " + maxQuality + ".");
        else
            System.err.println("Quality function equals " + maxQuality + ".");
        System.err.println("Final clustering consists of " + finalClustering.getNClusters() + " clusters.");

        // Write final clustering to file (or to standard output).
        System.err.println("Writing final clustering to " + ((finalClusteringFilename == null) ? "standard output." : "'" + finalClusteringFilename + "'."));
        writeClustering(finalClusteringFilename, finalClustering);
    }

    /**
     * Reads an edge list from a file and creates a network.
     *
     * @param filename       Filename
     * @param weightedEdges  Indicates whether edges have weights
     * @param sortedEdgeList Indicates whether the edge list is sorted
     *
     * @return Network
     */
    public static Network readEdgeList(String filename, boolean weightedEdges, boolean sortedEdgeList)
    {
        // Read edge list.
        DynamicIntArray[] edges = new DynamicIntArray[2];
        edges[0] = new DynamicIntArray(100);
        edges[1] = new DynamicIntArray(100);
        DynamicDoubleArray edgeWeights = weightedEdges ? new DynamicDoubleArray(100) : null;
        int nNodes = 0;
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            int lineNo = 0;
            while (line != null)
            {
                lineNo++;
                String[] columns = line.split(COLUMN_SEPARATOR);
                if ((!weightedEdges && ((columns.length < 2) || (columns.length > 3))) || (weightedEdges && (columns.length != 3)))
                    throw new IOException("Incorrect number of columns (line " + lineNo + ").");

                int node1;
                int node2;
                try
                {
                    node1 = Integer.parseUnsignedInt(columns[0]);
                    node2 = Integer.parseUnsignedInt(columns[1]);
                }
                catch (NumberFormatException e)
                {
                    throw new IOException("Node must be represented by a zero-index integer number (line " + lineNo + ").");
                }
                edges[0].append(node1);
                edges[1].append(node2);
                if (node1 >= nNodes)
                    nNodes = node1 + 1;
                if (node2 >= nNodes)
                    nNodes = node2 + 1;

                if (weightedEdges)
                {
                    double weight;
                    try
                    {
                        weight = Double.parseDouble(columns[2]);
                    }
                    catch (NumberFormatException e)
                    {
                        throw new IOException("Edge weight must be a number (line " + lineNo + ").");
                    }
                    edgeWeights.append(weight);
                }

                line = reader.readLine();
            }
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Error while reading edge list from file: File not found.");
            System.exit(-1);
        }
        catch (IOException e)
        {
            System.err.println("Error while reading edge list from file: " + e.getMessage());
            System.exit(-1);
        }
        finally
        {
            if (reader != null)
                try
                {
                    reader.close();
                }
                catch (IOException e)
                {
                    System.err.println("Error while reading edge list from file: " + e.getMessage());
                    System.exit(-1);
                }
        }

        // Create network.
        Network network = null;
        int[][] edges2 = new int[2][];
        edges2[0] = edges[0].toArray();
        edges2[1] = edges[1].toArray();
        try
        {
            if (weightedEdges)
                network = new Network(nNodes, true, edges2, edgeWeights.toArray(), sortedEdgeList, true);
            else
                network = new Network(nNodes, true, edges2, sortedEdgeList, true);
        }
        catch (IllegalArgumentException e)
        {
            System.err.println("Error while creating network: " + e.getMessage());
            System.exit(-1);
        }
        return network;
    }

    /**
     * Reads a clustering from a file.
     *
     * @param filename Filename
     * @param nNodes   Number of nodes
     *
     * @return Clustering
     */
    public static Clustering readClustering(String filename, int nNodes)
    {
        int[] clusters = new int[nNodes];
        Arrays.fill(clusters, -1);
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            int lineNo = 0;
            while (line != null)
            {
                lineNo++;
                String[] columns = line.split(COLUMN_SEPARATOR);
                if (columns.length != 2)
                    throw new IOException("Incorrect number of columns (line " + lineNo + ").");

                int node;
                try
                {
                    node = Integer.parseUnsignedInt(columns[0]);
                }
                catch (NumberFormatException e)
                {
                    throw new IOException("Node must be represented by a zero-index integer number (line " + lineNo + ").");
                }
                if (node >= nNodes)
                    throw new IOException("Invalid node (line " + lineNo + ").");
                int cluster;
                try
                {
                    cluster = Integer.parseUnsignedInt(columns[1]);
                }
                catch (NumberFormatException e)
                {
                    throw new IOException("Cluster must be represented by a zero-index integer number (line " + lineNo + ").");
                }
                if (clusters[node] >= 0)
                    throw new IOException("Duplicate node (line " + lineNo + ").");
                clusters[node] = cluster;

                line = reader.readLine();
            }
            if (lineNo < nNodes)
                throw new IOException("Missing nodes.");
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Error while reading clustering from file: File not found.");
            System.exit(-1);
        }
        catch (IOException e)
        {
            System.err.println("Error while reading clustering from file: " + e.getMessage());
            System.exit(-1);
        }
        finally
        {
            if (reader != null)
                try
                {
                    reader.close();
                }
                catch (IOException e)
                {
                    System.err.println("Error while reading clustering from file: " + e.getMessage());
                    System.exit(-1);
                }
        }

        return new Clustering(clusters);
    }

    /**
     * Writes a clustering to a file.
     *
     * @param filename   Filename
     * @param clustering Clustering
     */
    public static void writeClustering(String filename, Clustering clustering)
    {
        BufferedWriter writer = null;
        try
        {
            writer = new BufferedWriter((filename == null) ? new OutputStreamWriter(System.out) : new FileWriter(filename));
            for (int i = 0; i < clustering.getNNodes(); i++)
            {
                writer.write(i + COLUMN_SEPARATOR + clustering.getCluster(i));
                writer.newLine();
            }
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Error while writing clustering to file: File not found.");
            System.exit(-1);
        }
        catch (IOException e)
        {
            System.err.println("Error while writing clustering to file: " + e.getMessage());
            System.exit(-1);
        }
        finally
        {
            if (writer != null)
                try
                {
                    writer.close();
                }
                catch (IOException e)
                {
                    System.err.println("Error while writing clustering to file: " + e.getMessage());
                    System.exit(-1);
                }
        }
    }

    private RunNetworkClustering()
    {
    }
}
