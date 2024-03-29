# networkanalysis

[![Build master branch](https://github.com/CWTSLeiden/networkanalysis/workflows/Build%20master%20branch/badge.svg?branch=master)](https://github.com/CWTSLeiden/networkanalysis/actions)
[![License: MIT](https://badgen.net/github/license/CWTSLeiden/networkanalysis?label=License&color=yellow)](https://github.com/CWTSLeiden/networkanalysis/blob/master/LICENSE)
[![Latest release](https://badgen.net/github/release/CWTSLeiden/networkanalysis?label=Release)](https://github.com/CWTSLeiden/networkanalysis/releases)
[![Maven Central version](https://badgen.net/maven/v/maven-central/nl.cwts/networkanalysis)](https://central.sonatype.com/artifact/nl.cwts/networkanalysis)
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.1466830.svg)](https://doi.org/10.5281/zenodo.1466830)

## Introduction

This Java package provides algorithms and data structures for network analysis.
Currently, the package focuses on clustering (or community detection) and layout (or mapping) of networks.
In particular, the package contains an implementation of the [Leiden algorithm](https://arxiv.org/abs/1810.08473) and the [Louvain algorithm](https://arxiv.org/abs/0803.0476) for network clustering and the [VOS technique](https://arxiv.org/abs/1003.2551) for network layout.
Only undirected networks are supported.

The networkanalysis package was developed by [Nees Jan van Eck](https://orcid.org/0000-0001-8448-4521), [Vincent Traag](https://orcid.org/0000-0003-3170-3879), and [Ludo Waltman](https://orcid.org/0000-0001-8249-1752) at the [Centre for Science and Technology Studies (CWTS)](https://www.cwts.nl) at [Leiden University](https://www.universiteitleiden.nl/en).

## Documentation

Documentation is provided in the source code in `javadoc` format.
The documentation is also available in a [compiled format](https://CWTSLeiden.github.io/networkanalysis).

## Installation

### Maven

```
<dependency>
    <groupId>nl.cwts</groupId>
    <artifactId>networkanalysis</artifactId>
    <version>1.3.0</version>
</dependency>
```

### Gradle

```
implementation group: 'nl.cwts', name: 'networkanalysis', version: '1.3.0'
```

## Usage

The networkanalysis package requires Java 8 or higher.
The latest version of the package is available as a pre-compiled `jar` on [Maven Central](https://central.sonatype.com/artifact/nl.cwts/networkanalysis) and [GitHub Packages](https://github.com/CWTSLeiden/networkanalysis/packages).
Instructions for compiling the source code of the package are provided [below](#development-and-deployment).

To run the clustering algorithms, the command-line tool `RunNetworkClustering` is provided.
The tool can be run as follows:

```
java -cp networkanalysis-1.3.0.jar nl.cwts.networkanalysis.run.RunNetworkClustering
```

If no further arguments are provided, the following usage notice will be displayed:

```
RunNetworkClustering version 1.3.0
By Vincent Traag, Ludo Waltman, and Nees Jan van Eck
Centre for Science and Technology Studies (CWTS), Leiden University

Usage: RunNetworkClustering [options] <filename>

Identify clusters (also known as communities) in a network using either the
Leiden or the Louvain algorithm.

The file in <filename> is expected to contain a tab-separated edge list
(without a header line). Nodes are represented by zero-index integer numbers.
Only undirected networks are supported. Each edge should be included only once
in the file.

Options:
-q --quality-function {CPM|Modularity} (default: CPM)
    Quality function to be optimized. Either the CPM (constant Potts model) or
    the modularity quality function can be used.
-n --normalization {none|AssociationStrength|Fractionalization} (Default: none)
    Method for normalizing edge weights in the CPM quality function.
-r --resolution <resolution> (default: 1.0)
    Resolution parameter of the quality function.
-m --min-cluster-size <min. cluster size> (default: 1)
    Minimum number of nodes per cluster.
-a --algorithm {Leiden|Louvain} (default: Leiden)
    Algorithm for optimizing the quality function. Either the Leiden or the
    Louvain algorithm can be used.
-s --random-starts <random starts> (default: 1)
    Number of random starts of the algorithm.
-i --iterations <iterations> (default: 10)
    Number of iterations of the algorithm.
--randomness <randomness> (default: 0.01)
    Randomness parameter of the Leiden algorithm.
--seed <seed> (default: random)
    Seed of the random number generator.
-w --weighted-edges
    Indicates that the edge list file has a third column containing edge
    weights.
--sorted-edge-list
    Indicates that the edge list file is sorted. The file should be sorted based
    on the nodes in the first column, followed by the nodes in the second
    column. Each edge should be included in both directions in the file.
--input-clustering <filename> (default: singleton clustering)
    Read the initial clustering from the specified file. The file is expected to
    contain two tab-separated columns (without a header line), first a column of
    nodes and then a column of clusters. Nodes and clusters are both represented
    by zero-index integer numbers. If no file is specified, a singleton
    clustering (in which each node has its own cluster) is used as the initial
    clustering.
-o --output-clustering <filename> (default: standard output)
    Write the final clustering to the specified file. If no file is specified,
    the standard output is used.
```

To run the layout algorithm, the command-line tool `RunNetworkLayout` is provided.
The tool can be run as follows:

```
java -cp networkanalysis-1.3.0.jar nl.cwts.networkanalysis.run.RunNetworkLayout
```

If no further arguments are provided, the following usage notice will be displayed:

```
RunNetworkLayout version 1.3.0
By Nees Jan van Eck and Ludo Waltman
Centre for Science and Technology Studies (CWTS), Leiden University

Usage: RunNetworkLayout [options] <filename>

Determine a layout for a network using the gradient descent VOS layout
algorithm.

The file in <filename> is expected to contain a tab-separated edge list
(without a header line). Nodes are represented by zero-index integer numbers.
Only undirected networks are supported. Each edge should be included only once
in the file.

Options:
-q --quality-function {VOS|LinLog} (default: VOS)
    Quality function to be optimized. Either the VOS (visualization of
    similarities) or the LinLog quality function can be used.
-n --normalization {none|AssociationStrength|Fractionalization} (Default: none)
    Method for normalizing edge weights in the VOS quality function.
-a --attraction <attraction> (Default: 2)
    Attraction parameter of the VOS quality function.
-r --repulsion <repulsion> (Default: 1)
    Repulsion parameter of the VOS quality function.
-s --random-starts <random starts> (default: 1)
    Number of random starts of the gradient descent algorithm.
-i --max-iterations <max. iterations> (default: 1000)
    Maximum number of iterations of the gradient descent algorithm.
--initial-step-size <initial step size> (default: 1.0)
    Initial step size of the gradient descent algorithm.
--min-step-size <min. step size> (default: 0.001)
    Minimum step size of the gradient descent algorithm.
--step-size-reduction <step size reduction> (default: 0.75)
    Step size reduction of the gradient descent algorithm.
--required-quality-value-improvements <required quality value improvements>
        (default: 5)
    Required number of quality value improvements of the gradient descent
    algorithm.
--seed <seed> (default: random)
    Seed of the random number generator.
-w --weighted-edges
    Indicates that the edge list file has a third column containing edge
    weights.
--sorted-edge-list
    Indicates that the edge list file is sorted. The file should be sorted based
    on the nodes in the first column, followed by the nodes in the second
    column. Each edge should be included in both directions in the file.
--input-layout <filename> (default: random layout)
    Read the initial layout from the specified file. The file is expected to
    contain three tab-separated columns (without a header line), first a column
    of nodes, then a column of x coordinates, and finally a column of
    y coordinates. Nodes are represented by zero-index integer numbers. If no
    file is specified, a random layout (in which each node is positioned at
    random coordinates) is used as the initial layout.
-o --output-layout <filename> (default: standard output)
    Write the final layout to the specified file. If no file is specified,
    the standard output is used.
```

### Example

The following example illustrates the use of the `RunNetworkClustering` and `RunNetworkLayout` tools.
Consider this network:

```text
    0-----1
     \   /
      \ /
       2
       |
       3
      / \
     /   \
    4-----5
```

The network is encoded as an edge list that is saved in a text file containing two tab-separated columns:

```text
0	1
1	2
2	0
2	3
3	5
5	4
4	3
```

Nodes must be represented by integer numbers starting from 0.

Assuming that the edge list has been saved in the file `network.txt`, the `RunNetworkClustering` tool can be run as follows:

```
java -cp networkanalysis-1.3.0.jar nl.cwts.networkanalysis.run.RunNetworkClustering -r 0.2 -o clusters.txt network.txt
```

In this case, clusters are identified using the Leiden algorithm.
The CPM (constant Potts model) quality function is used without normalizing edge weights.
A value of `0.2` is used for the resolution parameter.
The resulting clustering is saved in the text file `clusters.txt` that contains two tab-separated columns:

```text
0	0
1	0
2	0
3	1
4	1
5	1
```

The file `clusters.txt` shows that two clusters have been identified.
The first column in the file represents a node, and the second column represents the cluster to which the node belongs.
Cluster 0 includes nodes 0, 1, and 2.
Cluster 1 includes nodes 3, 4, and 5.

The `RunNetworkLayout` tool can be run as follows:

```
java -cp networkanalysis-1.3.0.jar nl.cwts.networkanalysis.run.RunNetworkLayout -o layout.txt network.txt
```

In this case, the default parameter values are used for the VOS layout technique.
The resulting layout is saved in the text file `layout.txt` containing three tab-separated columns:

```text
0	-0.8690519467788094	-0.04001496992603245
1	-0.8690620214452673	0.040038034108640194
2	-0.4603890908313338	-2.5793522310420543E-5
3	0.46031975105512185	-1.6403462331212636E-5
4	0.8690853506388282	0.04007029704233864
5	0.86909795736146	-0.04005116424030402
```

The first column in the file `layout.txt` represents a node, and the second and third column represent the x and y coordinates of the node.

In the above example, the edges in the file `network.txt` have not been sorted.
To provide a sorted edge list as input, include the edges in both directions and use the option ``--sorted-edge-list``.
Furthermore, edge weights can be provided by adding a third column to the file `network.txt` and by using the option ``--weighted-edges``.

## License

The networkanalysis package is distributed under the [MIT license](LICENSE).

## Issues

If you encounter any issues, please report them using the [issue tracker](https://github.com/CWTSLeiden/networkanalysis/issues) on GitHub.

## Contribution

You are welcome to contribute to the development of the networkanalysis package.
Please follow the typical GitHub workflow: Fork from this repository and make a pull request to submit your changes.
Make sure that your pull request has a clear description and that your source code has been properly tested.

## Development and deployment

The latest stable version of the source code is available in the [`master`](https://github.com/CWTSLeiden/networkanalysis/tree/master) branch on GitHub.
The most recent version of the source code, which may be under development, is available in the [`develop`](https://github.com/CWTSLeiden/networkanalysis/tree/develop) branch.

### Compilation

To compile the source code of the networkanalysis package, a [Java Development Kit](https://jdk.java.net) needs to be installed on your system (version 8 or higher). Having [Gradle](https://www.gradle.org) installed is optional as the [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) is also included in this repository.

On Windows, the source code can be compiled as follows:

```
gradlew build
```

On Linux and MacOS, use the following command:

```
./gradlew build
```

The compiled `class` files will be output to the directory `build/classes`.
The compiled `jar` file will be output to the directory `build/libs`.
The compiled `javadoc` files will be output to the directory `build/docs`.

There are two `main` methods, one in the class `nl.cwts.networkanalysis.run.RunNetworkClustering` and one in the class `nl.cwts.networkanalysis.run.RunNetworkLayout`.
After compiling the source code, the `RunNetworkClustering` tool can be run as follows:

```
java -cp build/libs/networkanalysis-<version>.jar nl.cwts.networkanalysis.run.RunNetworkClustering
```

The `RunNetworkLayout` tool can be run as follows:

```
java -cp build/libs/networkanalysis-<version>.jar nl.cwts.networkanalysis.run.RunNetworkLayout
```

## References

> Traag, V.A., Waltman, L., & Van Eck, N.J. (2019). From Louvain to Leiden: Guaranteeing well-connected communities. *Scientific Reports*, 9, 5233. https://doi.org/10.1038/s41598-019-41695-z

> Van Eck, N.J., Waltman, L., Dekker, R., & Van den Berg, J. (2010). A comparison of two techniques for bibliometric mapping: Multidimensional scaling and VOS. *Journal of the American Society for Information Science and Technology*, 61(12), 2405-2416. https://doi.org/10.1002/asi.21421

> Waltman, L., Van Eck, N.J., & Noyons, E.C.M. (2010). A unified approach to mapping and clustering of bibliometric networks. *Journal of Informetrics*, 4(4), 629-635. https://doi.org/10.1016/j.joi.2010.07.002

> Van Eck, N.J., & Waltman, L. (2009). How to normalize co-occurrence data? An analysis of some well-known similarity measures. *Journal of the American Society for Information Science and Technology*, 60(8), 1635-1651. https://doi.org/10.1002/asi.21075
