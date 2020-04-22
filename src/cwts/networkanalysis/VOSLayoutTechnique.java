package cwts.networkanalysis;

/**
 * VOSLayoutTechnique
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 */

import java.util.Arrays;
import java.util.Random;

public class VOSLayoutTechnique
{
    protected Network network;
    protected Layout layout;
    protected int attraction;
    protected int repulsion;
    protected double edgeWeightIncrement;

    private static double pow(double base, int exponent)
    {
        double power;
        int i;

        if (exponent > 0)
        {
            power = base;
            for (i = 1; i < exponent; i++)
                power *= base;
        }
        else if (exponent < 0)
        {
            power = 1 / base;
            for (i = -1; i > exponent; i--)
                power /= base;
        }
        else
            power = 1;
        return power;
    }

    public VOSLayoutTechnique(Network network, int attraction, int repulsion, double edgeWeightIncrement)
    {
        this(network, attraction, repulsion, edgeWeightIncrement, new Random());
    }

    public VOSLayoutTechnique(Network network, int attraction, int repulsion, double edgeWeightIncrement, Random random)
    {
        this.network = network;
        layout = new Layout(network.nNodes);
        layout.initRandomCoordinates(random);
        this.attraction = attraction;
        this.repulsion = repulsion;
        this.edgeWeightIncrement = edgeWeightIncrement;
    }

    public VOSLayoutTechnique(Network network, Layout layout, int attraction, int repulsion, double edgeWeightIncrement)
    {
        this.network = network;
        this.layout = layout;
        this.attraction = attraction;
        this.repulsion = repulsion;
        this.edgeWeightIncrement = edgeWeightIncrement;
    }

    public Network getNetwork()
    {
        return network;
    }

    public Layout getLayout()
    {
        return layout;
    }

    public int getAttraction()
    {
        return attraction;
    }

    public int getRepulsion()
    {
        return repulsion;
    }

    public double getEdgeWeightIncrement()
    {
        return edgeWeightIncrement;
    }

    public void setNetwork(Network network)
    {
        this.network = network;
    }

    public void setLayout(Layout layout)
    {
        this.layout = layout;
    }

    public void setAttraction(int attraction)
    {
        this.attraction = attraction;
    }

    public void setRepulsion(int repulsion)
    {
        this.repulsion = repulsion;
    }

    public void setEdgeWeightIncrement(double edgeWeightIncrement)
    {
        this.edgeWeightIncrement = edgeWeightIncrement;
    }

    public double calcQualityFunction()
    {
        double distance, distance1, distance2, qualityFunction;
        int i, j;

        qualityFunction = 0;

        for (i = 0; i < network.nNodes; i++)
            for (j = network.firstNeighborIndex[i]; j < network.firstNeighborIndex[i + 1]; j++)
                if (network.neighbor[j] < i)
                {
                    distance1 = layout.coordinate[0][i] - layout.coordinate[0][network.neighbor[j]];
                    distance2 = layout.coordinate[1][i] - layout.coordinate[1][network.neighbor[j]];
                    distance = Math.sqrt(distance1 * distance1 + distance2 * distance2);
                    if (attraction != 0)
                        qualityFunction += network.edgeWeight[j] * pow(distance, attraction) / attraction;
                    else
                        qualityFunction += network.edgeWeight[j] * Math.log(distance);
                }

        for (i = 0; i < network.nNodes; i++)
            for (j = 0; j < i; j++)
            {
                distance1 = layout.coordinate[0][i] - layout.coordinate[0][j];
                distance2 = layout.coordinate[1][i] - layout.coordinate[1][j];
                distance = Math.sqrt(distance1 * distance1 + distance2 * distance2);
                if (repulsion != 0)
                    qualityFunction -= network.nodeWeight[i] * network.nodeWeight[j] * pow(distance, repulsion) / repulsion;
                else
                    qualityFunction -= network.nodeWeight[i] * network.nodeWeight[j] * Math.log(distance);
            }

        if (edgeWeightIncrement > 0)
            for (i = 0; i < network.nNodes; i++)
                for (j = 0; j < i; j++)
                {
                    distance1 = layout.coordinate[0][i] - layout.coordinate[0][j];
                    distance2 = layout.coordinate[1][i] - layout.coordinate[1][j];
                    distance = Math.sqrt(distance1 * distance1 + distance2 * distance2);
                    if (attraction != 0)
                        qualityFunction += edgeWeightIncrement * pow(distance, attraction) / attraction;
                    else
                        qualityFunction += edgeWeightIncrement * Math.log(distance);
                }

        return qualityFunction;
    }

    public double runGradientDescentAlgorithm(int maxNIterations, double initialStepLength, double minStepLength, double stepLengthReduction, int requiredNQualityFunctionImprovements)
    {
        return runGradientDescentAlgorithm(maxNIterations, initialStepLength, minStepLength, stepLengthReduction, requiredNQualityFunctionImprovements, new Random());
    }

    public double runGradientDescentAlgorithm(int maxNIterations, double initialStepLength, double minStepLength, double stepLengthReduction, int requiredNQualityFunctionImprovements, Random random)
    {
        boolean[] nodeVisited;
        double a, b, distance, distance1, distance2, gradient1, gradient2, gradientLength, qualityFunction, qualityFunctionOld, squaredDistance, stepLength;
        int i, j, k, l, nQualityFunctionImprovements;
        int[] nodePermutation;

        nodePermutation = Arrays2.generateRandomPermutation(network.nNodes, random);

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

                for (l = network.firstNeighborIndex[k]; l < network.firstNeighborIndex[k + 1]; l++)
                {
                    distance1 = layout.coordinate[0][k] - layout.coordinate[0][network.neighbor[l]];
                    distance2 = layout.coordinate[1][k] - layout.coordinate[1][network.neighbor[l]];
                    squaredDistance = distance1 * distance1 + distance2 * distance2;

                    distance = Math.sqrt(squaredDistance);
                    a = pow(distance, attraction);

                    if (squaredDistance > 0)
                    {
                        b = network.edgeWeight[l] * a / squaredDistance;
                        gradient1 += b * distance1;
                        gradient2 += b * distance2;
                    }

                    if (!nodeVisited[network.neighbor[l]])
                        if (attraction != 0)
                            qualityFunction += network.edgeWeight[l] * a / attraction;
                        else
                            qualityFunction += network.edgeWeight[l] * Math.log(distance);
                }

                for (l = 0; l < network.nNodes; l++)
                    if (l != k)
                    {
                        distance1 = layout.coordinate[0][k] - layout.coordinate[0][l];
                        distance2 = layout.coordinate[1][k] - layout.coordinate[1][l];
                        squaredDistance = distance1 * distance1 + distance2 * distance2;
                        distance = Math.sqrt(squaredDistance);
                        a = pow(distance, repulsion);

                        if (squaredDistance > 0)
                        {
                            b = network.nodeWeight[k] * network.nodeWeight[l] * a / squaredDistance;
                            gradient1 -= b * distance1;
                            gradient2 -= b * distance2;
                        }

                        if (!nodeVisited[l])
                            if (repulsion != 0)
                                qualityFunction -= network.nodeWeight[k] * network.nodeWeight[l] * a / repulsion;
                            else
                                qualityFunction -= network.nodeWeight[k] * network.nodeWeight[l] * Math.log(distance);
                    }

                if (edgeWeightIncrement > 0)
                    for (l = 0; l < network.nNodes; l++)
                        if (l != k)
                        {
                            distance1 = layout.coordinate[0][k] - layout.coordinate[0][l];
                            distance2 = layout.coordinate[1][k] - layout.coordinate[1][l];
                            squaredDistance = distance1 * distance1 + distance2 * distance2;
                            distance = Math.sqrt(squaredDistance);
                            a = pow(distance, attraction);

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
                layout.coordinate[0][k] -= stepLength * gradient1 / gradientLength;
                layout.coordinate[1][k] -= stepLength * gradient2 / gradientLength;

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

        return stepLength;
    }
}
