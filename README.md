[![Build master branch](https://github.com/CWTSLeiden/networkanalysis/workflows/Build%20master%20branch/badge.svg?branch=master)](https://github.com/CWTSLeiden/networkanalysis/actions)
[![License: MIT](https://badgen.net/github/license/CWTSLeiden/networkanalysis?label=License&color=yellow)](https://github.com/CWTSLeiden/networkanalysis/blob/master/LICENSE)
[![Latest release](https://badgen.net/github/release/CWTSLeiden/networkanalysis?label=Release)](https://github.com/CWTSLeiden/networkanalysis/releases)
[![DOI](https://zenodo.org/badge/153760626.svg)](https://zenodo.org/badge/latestdoi/153760626)

## Introduction

This package provides algorithms and data structures for network analysis in `java`.
Currently, the package focuses on clustering (or community detection) and layout (or mapping) of networks.
In particular, the package contains an implementation of the [Leiden algorithm](https://arxiv.org/abs/1810.08473) and the [Louvain algorithm](https://arxiv.org/abs/0803.0476) for network clustering and the [VOS technique](https://arxiv.org/abs/1003.2551) for network layout.
Only undirected networks are supported.

This package requires `java 1.8.0` or higher.

## Usage

The latest version of this package is available as a pre-compiled `jar` file in the GitHub [release](https://github.com/CWTSLeiden/networkanalysis/releases/latest).
The source code is also available in this repository.
You can use it to [compile](#compilation) the code yourself.

To run the clustering algorithms, the command-line tool `RunNetworkClustering` is provided.
The tool can be run as follows:

```
java -cp networkanalysis-1.1.0.jar nl.cwts.networkanalysis.run.RunNetworkClustering
```

If no further arguments are provided, the following usage notice will be displayed:

```
RunNetworkClustering version 1.1.0
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
java -cp networkanalysis-1.1.0.jar nl.cwts.networkanalysis.run.RunNetworkLayout
```

If no further arguments are provided, the following usage notice will be displayed:

```
RunNetworkLayout version 1.1.0
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
java -cp networkanalysis-1.1.0.jar nl.cwts.networkanalysis.run.RunNetworkClustering -r 0.2 -o clusters.txt network.txt
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
java -cp networkanalysis-1.1.0.jar nl.cwts.networkanalysis.run.RunNetworkLayout -o layout.txt network.txt
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

## Compilation

You must have JDK 1.8+ installed to compile.
Having Gradle installed is optional as the [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) is also included in this repository.
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
After the code has been compiled, the `RunNetworkClustering` tool can be run as follows:

```
java -cp build/libs/networkanalysis-<version>.jar nl.cwts.networkanalysis.run.RunNetworkClustering
```

The `RunNetworkLayout` tool can be run as follows:

```
java -cp build/libs/networkanalysis-<version>.jar nl.cwts.networkanalysis.run.RunNetworkLayout
```

The latest stable version of the code is available from the [`master`](https://github.com/CWTSLeiden/networkanalysis/tree/master) branch on GitHub.
The most recent code, which may be under development, is available from the [`develop`](https://github.com/CWTSLeiden/networkanalysis/tree/develop) branch.

## Issues

If you encounter any issues, please report them using the [issue tracker](https://github.com/CWTSLeiden/networkanalysis/issues).

## Documentation

Documentation of the source code is provided in the code in `javadoc` format.
The documentation is also available in a [compiled format](https://CWTSLeiden.github.io/networkanalysis).

## Contribution

You are welcome to contribute to this package.
Please follow the typical GitHub workflow: fork from this repository and make a pull request to submit your changes.
At the moment, we have not yet set up any continuous integration, so please make sure that any proposed pull request compiles and functions correctly.

## License

This package is distributed under the MIT License.
Please refer to the [`LICENSE`](LICENSE) file for further details.
