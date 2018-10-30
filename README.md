## Introduction

This package provides data structures and algorithms for network analysis in `java`.
Currently, the focus of the package is restricted to clustering (or community detection) in networks.
In particular, the package contains an implementation of the [Leiden algorithm](https://arxiv.org/abs/xxx.xxxx) and the [Louvain algorithm](https://arxiv.org/abs/0803.0476).
Only undirected networks are supported.

[![DOI](https://zenodo.org/badge/153760626.svg)](https://zenodo.org/badge/latestdoi/153760626)

## Usage

To run the clustering algorithms, the command-line tool `RunNetworkClustering` is provided.
The latest version of the tool is available as a pre-compiled `jar` file in the GitHub [release](https://github.com/CWTSLeiden/networkanalysis/releases/latest).
The source code is also available in this repository.
You can use it to [compile](#compilation) the code yourself.
The `.jar` file can be executed as follows:

```
java -jar RunNetworkClustering.jar
```

If no further arguments are provided, the following usage notice will be displayed:

```
Usage: RunNetworkClustering [options] <filename>

Identify clusters (also known as communities) in a network, using either the
Leiden or the Louvain algorithm.

The file in <filename> is expected to contain a tab-separated edge list
(without a header line). Nodes are represented by zero-index integer numbers.
Only undirected networks are supported. Each edge should be included only once
in the file.

Options:
-q --quality-function {CPM|modularity} (default: CPM)
    Quality function to be optimized. Either the CPM (constant Potts model) or
    the modularity quality function can be used.
-r --resolution <resolution> (default: 1.0)
    Resolution parameter of the quality function.
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

To run the clustering algorithms, you need `java 1.8.0` or higher.

### Example

The following example illustrates the use of the `RunNetworkClustering` tool.
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

The network is encoded as an edge list that is saved in a tab-separated text file:

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
java -jar RunNetworkClustering.jar -r 0.2 -o clusters.txt network.txt
```

In this case, clusters are identified using the Leiden algorithm based on the CPM quality function with a value of `0.2` for the resolution parameter.
The resulting clustering is saved in the text file `clusters.txt`:

```text
0	0
1	0
2	0
3	1
4	1
5	1
```

The file `clusters.txt` shows that two clusters have been identified.
The first column in `clusters.txt` indicates the node, and the second column indicates the cluster to which the node belongs.
Cluster 0 includes nodes 0, 1, and 2.
Cluster 1 includes nodes 3, 4, and 5.
In the above example, the edges in the file `network.txt` have not been sorted.
To provide a sorted edge list as input, include the edges in both directions and use the option ``--sorted-edge-list``.
Furthermore, edge weights can be provided by adding a third column to the file `network.txt` and by using the option ``--weighted-edges``.

## Compilation

The source code can be compiled as follows:

```
javac -d build src/cwts/networkanalysis/*.java src/cwts/networkanalysis/run/*.java src/cwts/util/*.java
```

The compiled `class` files will be output to the directory `build`.
There are no external dependencies.
The `main` method is provided in the class `cwts.networkanalysis.run.RunNetworkClustering`.
After the code has been compiled, the `RunNetworkClustering` tool can be run as follows:

```
java -cp build cwts.networkanalysis.run.RunNetworkClustering
```

The latest stable version of the code is available from the [`master`](https://github.com/CWTSLeiden/networkanalysis/tree/master) branch on GitHub.
The most recent code, which may be under development, is available from the [`develop`](https://github.com/CWTSLeiden/networkanalysis/tree/develop) branch.

## Issues

If you encounter any issues, please report them using the [issue tracker](https://github.com/CWTSLeiden/networkanalysis/issues).
Before submitting, please examine whether issues have not yet been reported before.

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
