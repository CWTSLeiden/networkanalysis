package cwts.networkanalysis;

import java.nio.file.Files;
import java.io.File;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Abstract base class for iterative clustering algorithms that use the CPM
 * quality function.
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 * @author Vincent Traag
 */
public abstract class IterativeCPMClusteringAlgorithm extends IncrementalCPMClusteringAlgorithm
{
    /**
     * Default number of iterations.
     */
    public static final int DEFAULT_N_ITERATIONS = 1;

    /**
     * Number of iterations.
     */
    protected int nIterations;

    /**
     * Constructs an iterative CPM clustering algorithm.
     */
    public IterativeCPMClusteringAlgorithm()
    {
        this(DEFAULT_RESOLUTION, DEFAULT_N_ITERATIONS);
    }

    /**
     * Constructs an iterative CPM clustering algorithm with a specified
     * resolution parameter and number of iterations.
     *
     * @param resolution  Resolution parameter
     * @param nIterations Number of iterations
     */
    public IterativeCPMClusteringAlgorithm(double resolution, int nIterations)
    {
        super(resolution);

        this.nIterations = nIterations;
    }

    /**
     * Returns the number of iterations.
     *
     * @return Number of iterations
     */
    public int getNIterations()
    {
        return nIterations;
    }

    /**
     * Sets the number of iterations.
     *
     * @param nIterations Number of iterations
     */
    public void setNIterations(int nIterations)
    {
        this.nIterations = nIterations;
    }

    /**
     * Improves a clustering of the nodes in a network.
     *
     * <p>
     * If the number of iterations {@code nIterations} is positive, the
     * clustering is improved by making {@code nIterations} calls to {@link
     * #improveClusteringOneIteration(Network network, Clustering clustering)}.
     * If {@code nIterations} equals 0, calls to {@link
     * #improveClusteringOneIteration(Network network, Clustering clustering)}
     * continue to be made until there has been a call that did not result in
     * an improvement of the clustering.
     * </p>
     *
     * @param network    Network
     * @param clustering Clustering
     *
     * @return Boolean indicating whether the clustering has been improved
     */
    public boolean improveClustering(Network network, Clustering clustering)
    {
        boolean update;
        int i;

        update = false;
        if (nIterations > 1) {
            String measurement = "duration quality";
            long time = 0;
            for (i = 0; i < nIterations; i++) {
                long start = System.nanoTime();
                update |= improveClusteringOneIteration(network, clustering);
                long duration = System.nanoTime() - start;
                time += duration;
                measurement = measurement + "\n" + time;
                double quality = calcQuality(network, clustering);
                String qualityStr = " " + quality;
                measurement = measurement + qualityStr.replace('.', ',');
            }
            System.out.println(measurement);
            String filename = "measurements.txt";
            Path path = Paths.get(filename);
            if (Files.notExists(path)) {
                File file = new File(filename);
                try {
                    file.createNewFile();
                }
                catch (Exception e) {
                    System.err.println(e);
                }
            }
            if (Files.exists(path)) {
                try {
                    Files.write(Paths.get(filename), measurement.getBytes(), StandardOpenOption.APPEND);
                }catch (Exception e) {
                    System.err.println(e);
                }
            }
        }
        else if(nIterations == 1) update |= improveClusteringOneIteration(network, clustering);
        else {
            while (improveClusteringOneIteration(network, clustering))
                update = true;
        }
        return update;
    }

    /**
     * Improves a clustering by performing one iteration of an iterative
     * clustering algorithm.
     *
     * @param network    Network
     * @param clustering Clustering
     *
     * @return Boolean indicating whether the clustering has been improved
     */
    protected abstract boolean improveClusteringOneIteration(Network network, Clustering clustering);
}
