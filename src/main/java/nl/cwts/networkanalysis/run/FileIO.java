package nl.cwts.networkanalysis.run;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;

import nl.cwts.networkanalysis.Clustering;
import nl.cwts.networkanalysis.Layout;
import nl.cwts.networkanalysis.Network;
import nl.cwts.util.LargeDoubleArray;
import nl.cwts.util.LargeIntArray;

/**
 * Utility functions for file I/O.
 *
 * <p>
 * All methods in this class are static.
 * </p>
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 * @author Vincent Traag
 */
public class FileIO
{
    /**
     * Column separator for edge list, clustering, and layout files.
     */
    public static final String COLUMN_SEPARATOR = "\t";

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
        LargeIntArray[] edges = new LargeIntArray[2];
        edges[0] = new LargeIntArray(0); edges[0].ensureCapacity(100);
        edges[1] = new LargeIntArray(0); edges[1].ensureCapacity(100);
        LargeDoubleArray edgeWeights = weightedEdges ? new LargeDoubleArray(0) : null;
        if (edgeWeights != null)
            edgeWeights.ensureCapacity(100);
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
        try
        {
            if (weightedEdges)
                network = new Network(nNodes, true, edges, edgeWeights, sortedEdgeList, true);
            else
                network = new Network(nNodes, true, edges, sortedEdgeList, true);
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

    /**
     * Reads a layout from a file.
     *
     * @param filename Filename
     * @param nNodes   Number of nodes
     *
     * @return Layout
     */
    public static Layout readLayout(String filename, int nNodes)
    {
        double[][] coordinates = new double[2][nNodes];
        boolean[] hasCoordinates = new boolean[nNodes];
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
                if (columns.length != 3)
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
                double x;
                double y;
                try
                {
                    x = Double.parseDouble(columns[1]);
                    y = Double.parseDouble(columns[2]);
                }
                catch (NumberFormatException e)
                {
                    throw new IOException("Coordinates must be numbers (line " + lineNo + ").");
                }
                if (hasCoordinates[node])
                    throw new IOException("Duplicate node (line " + lineNo + ").");
                coordinates[0][node] = x;
                coordinates[1][node] = y;
                hasCoordinates[node] = true;

                line = reader.readLine();
            }
            if (lineNo < nNodes)
                throw new IOException("Missing nodes.");
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Error while reading layout from file: File not found.");
            System.exit(-1);
        }
        catch (IOException e)
        {
            System.err.println("Error while reading layout from file: " + e.getMessage());
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
                    System.err.println("Error while reading layout from file: " + e.getMessage());
                    System.exit(-1);
                }
        }

        return new Layout(coordinates);
    }

    /**
     * Writes a layout to a file.
     *
     * @param filename Filename
     * @param layout   Layout
     */
    public static void writeLayout(String filename, Layout layout)
    {
        BufferedWriter writer = null;
        try
        {
            writer = new BufferedWriter((filename == null) ? new OutputStreamWriter(System.out) : new FileWriter(filename));
            for (int i = 0; i < layout.getNNodes(); i++)
            {
                double[] coordinates = layout.getCoordinates(i);
                writer.write(i + COLUMN_SEPARATOR + coordinates[0] + COLUMN_SEPARATOR + coordinates[1]);
                writer.newLine();
            }
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Error while writing layout to file: File not found.");
            System.exit(-1);
        }
        catch (IOException e)
        {
            System.err.println("Error while writing layout to file: " + e.getMessage());
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
                    System.err.println("Error while writing layout to file: " + e.getMessage());
                    System.exit(-1);
                }
        }
    }

    private FileIO()
    {
    }
}
