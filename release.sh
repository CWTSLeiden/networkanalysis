#!/bin/bash
VERSION=`git describe --tags`
printf "Building for version $VERSION\n"
printf "Make sure versions match also in documentation and source code.\n"

printf "\nBuilding java class files\n"
printf "================================================\n"
javac -d build/jar src/cwts/networkanalysis/*.java src/cwts/networkanalysis/run/*.java src/cwts/util/*.java

printf "\nPackaging jar file\n"
printf "================================================\n"
jar cvfe dist/RunNetworkClustering.jar cwts.networkanalysis.run.RunNetworkClustering -C build/jar .

printf "\nPackaging source code\n"
printf "================================================\n"
find src -type f -iname "*.java" -print0 | tar -czvf dist/RunNetworkClustering-source.tar.gz --null -T -

printf "\nBuilding javadoc\n"
printf "================================================\n"
javadoc -noindex -d docs/ src/cwts/networkanalysis/*.java src/cwts/util/*.java
