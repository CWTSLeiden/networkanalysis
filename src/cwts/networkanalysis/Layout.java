package cwts.networkanalysis;

/**
 * Layout
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 */

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Random;

public class Layout implements Cloneable, Serializable
{
    private static final long serialVersionUID = 1;

    protected int nNodes;
    protected double[][] coordinate;

    public static Layout load(String fileName) throws ClassNotFoundException, IOException
    {
        Layout layout;
        ObjectInputStream objectInputStream;

        objectInputStream = new ObjectInputStream(new FileInputStream(fileName));

        layout = (Layout)objectInputStream.readObject();

        objectInputStream.close();

        return layout;
    }

    public Layout(int nNodes)
    {
        this.nNodes = nNodes;
        coordinate = new double[2][nNodes];
    }

    public Layout(double[][] coordinate)
    {
        nNodes = coordinate[0].length;
        this.coordinate = new double[2][];
        this.coordinate[0] = coordinate[0].clone();
        this.coordinate[1] = coordinate[1].clone();
    }

    public Layout clone()
    {
        Layout clonedLayout;

        try
        {
            clonedLayout = (Layout)super.clone();
            clonedLayout.coordinate = new double[2][];
            clonedLayout.coordinate[0] = coordinate[0].clone();
            clonedLayout.coordinate[1] = coordinate[1].clone();
            return clonedLayout;
        }
        catch (CloneNotSupportedException e)
        {
            return null;
        }
    }

    public void save(String fileName) throws IOException
    {
        ObjectOutputStream objectOutputStream;

        objectOutputStream = new ObjectOutputStream(new FileOutputStream(fileName));

        objectOutputStream.writeObject(this);

        objectOutputStream.close();
    }

    public int getNNodes()
    {
        return nNodes;
    }

    public double[][] getCoordinates()
    {
        double[][] clonedCoordinate;

        clonedCoordinate = new double[2][];
        clonedCoordinate[0] = coordinate[0].clone();
        clonedCoordinate[1] = coordinate[1].clone();
        return clonedCoordinate;
    }

    public double[] getCoordinates(int node)
    {
        double[] coordinate;

        coordinate = new double[2];
        coordinate[0] = this.coordinate[0][node];
        coordinate[1] = this.coordinate[1][node];
        return coordinate;
    }

    public double[] getMinCoordinates()
    {
        double[] minCoordinate;

        minCoordinate = new double[2];
        minCoordinate[0] = Arrays2.calcMinimum(coordinate[0]);
        minCoordinate[1] = Arrays2.calcMinimum(coordinate[1]);
        return minCoordinate;
    }

    public double[] getMaxCoordinates()
    {
        double[] maxCoordinate;

        maxCoordinate = new double[2];
        maxCoordinate[0] = Arrays2.calcMaximum(coordinate[0]);
        maxCoordinate[1] = Arrays2.calcMaximum(coordinate[1]);
        return maxCoordinate;
    }

    public double getAverageDistance()
    {
        double averageDistance, distance1, distance2;
        int i, j;

        averageDistance = 0;
        for (i = 0; i < nNodes; i++)
            for (j = 0; j < i; j++)
            {
                distance1 = coordinate[0][i] - coordinate[0][j];
                distance2 = coordinate[1][i] - coordinate[1][j];
                averageDistance += Math.sqrt(distance1 * distance1 + distance2 * distance2);
            }
        averageDistance /= nNodes * (nNodes - 1) / 2;
        return averageDistance;
    }

    public void setCoordinates(int node, double[] coordinate)
    {
        this.coordinate[0][node] = coordinate[0];
        this.coordinate[1][node] = coordinate[1];
    }

    public void initRandomCoordinates()
    {
        initRandomCoordinates(new Random());
    }

    public void initRandomCoordinates(Random random)
    {
        int i;

        for (i = 0; i < nNodes; i++)
        {
            coordinate[0][i] = 2 * random.nextDouble() - 1;
            coordinate[1][i] = 2 * random.nextDouble() - 1;
        }
    }

    public void standardizeCoordinates(boolean standardizeDistances)
    {
        double averageCoordinate1, averageCoordinate2, averageDistance, coordinateOld1, coordinateOld2, covariance, discriminant, eigenvalue1, eigenvalue2, normalizedEigenvector11, normalizedEigenvector12, normalizedEigenvector21, normalizedEigenvector22, variance1, variance2, vectorLength;
        int i, j;

        averageCoordinate1 = Arrays2.calcAverage(coordinate[0]);
        averageCoordinate2 = Arrays2.calcAverage(coordinate[1]);
        for (i = 0; i < nNodes; i++)
        {
            coordinate[0][i] -= averageCoordinate1;
            coordinate[1][i] -= averageCoordinate2;
        }

        variance1 = 0;
        variance2 = 0;
        covariance = 0;
        for (i = 0; i < nNodes; i++)
        {
            variance1 += coordinate[0][i] * coordinate[0][i];
            variance2 += coordinate[1][i] * coordinate[1][i];
            covariance += coordinate[0][i] * coordinate[1][i];
        }
        variance1 /= nNodes;
        variance2 /= nNodes;
        covariance /= nNodes;
        discriminant = variance1 * variance1 + variance2 * variance2 - 2 * variance1 * variance2 + 4 * covariance * covariance;
        eigenvalue1 = (variance1 + variance2 - Math.sqrt(discriminant)) / 2;
        eigenvalue2 = (variance1 + variance2 + Math.sqrt(discriminant)) / 2;
        normalizedEigenvector11 = variance1 + covariance - eigenvalue1;
        normalizedEigenvector12 = variance2 + covariance - eigenvalue1;
        vectorLength = Math.sqrt(normalizedEigenvector11 * normalizedEigenvector11 + normalizedEigenvector12 * normalizedEigenvector12);
        normalizedEigenvector11 /= vectorLength;
        normalizedEigenvector12 /= vectorLength;
        normalizedEigenvector21 = variance1 + covariance - eigenvalue2;
        normalizedEigenvector22 = variance2 + covariance - eigenvalue2;
        vectorLength = Math.sqrt(normalizedEigenvector21 * normalizedEigenvector21 + normalizedEigenvector22 * normalizedEigenvector22);
        normalizedEigenvector21 /= vectorLength;
        normalizedEigenvector22 /= vectorLength;
        for (i = 0; i < nNodes; i++)
        {
            coordinateOld1 = coordinate[0][i];
            coordinateOld2 = coordinate[1][i];
            coordinate[0][i] = normalizedEigenvector11 * coordinateOld1 + normalizedEigenvector12 * coordinateOld2;
            coordinate[1][i] = normalizedEigenvector21 * coordinateOld1 + normalizedEigenvector22 * coordinateOld2;
        }

        for (i = 0; i < 2; i++)
            if (Arrays2.calcMedian(coordinate[i]) > 0)
                for (j = 0; j < nNodes; j++)
                    coordinate[i][j] *= -1;

        if (standardizeDistances)
        {
            averageDistance = getAverageDistance();
            for (i = 0; i < nNodes; i++)
            {
                coordinate[0][i] /= averageDistance;
                coordinate[1][i] /= averageDistance;
            }
        }
    }

    public void rotate(double angle)
    {
        double coordinateOld1, coordinateOld2, cos, sin;
        int i;

        sin = Math.sin(-angle * Math.PI / 180);
        cos = Math.cos(-angle * Math.PI / 180);
        for (i = 0; i < nNodes; i++)
        {
            coordinateOld1 = coordinate[0][i];
            coordinateOld2 = coordinate[1][i];
            coordinate[0][i] = cos * coordinateOld1 - sin * coordinateOld2;
            coordinate[1][i] = sin * coordinateOld1 + cos * coordinateOld2;
        }
    }

    public void flip(int dimension)
    {
        int i;

        for (i = 0; i < nNodes; i++)
            coordinate[dimension][i] *= -1;
    }
}
