maven-dependency-analyser
=========================

Dot Graph generator for maven dependencies in hosted repositories

![Example Output](https://github-camo.global.ssl.fastly.net/f0f947fe53944c3d3dc7cffb5b311c3d0f75dab4/687474703a2f2f692e696d6775722e636f6d2f625a38643933502e706e67)

Build Instructions
------------------
Currently this broject is build using [Apache Maven](http://maven.apache.org/ "Maven Site"). To build and run the analyser, follow these 3 easy steps:

1. In the root of the project, run a Maven clean install: ``` mvn clean install ```
2. Move into the target directory: ``` cd target ```
3. Run the jar file generated: ``` java -jar <resource.jar> ```

Runtime Options
---------------

The analyser takes 5 possible arguments:  
```
usage: AnalyserCLI
  --file <arg>          File to read repos from
  --password <arg>      Password for web requests (if needed)
  --printTest <arg>     Include test dependencies
  --restriction <arg>   Artifact restriction
  --username <arg>      Username for web requests (if needed)
```  
*Note:* Specifying a file is mandatory, this file lists the repository bases to search when establishing dependencies.  

Example usage
-------------
The following example shows how to populate a file such that the analyser will correctly parse your dependencies:

To parse a POM file located at 
[https://raw.github.com/pwhittlesea/maven-dependency-analyser/master/pom.xml](https://raw.github.com/pwhittlesea/maven-dependency-analyser/master/pom.xml "Example POM")
you would create a file with the following contents
```
https://raw.github.com/pwhittlesea/maven-dependency-analyser/master/

```

This would generate output similar to the following:
```
digraph G {
    size="1000,1000";
    subgraph cluster_0 {
        label="org.codehaus.plexus";
        color=blue;
        style=dashed;
        Node_0 [label="plexus-utils", shape=box];
    }
    subgraph cluster_1 {
        label="uk.me.thega";
        color=green;
        style=dashed;
        Node_1 [label="analyser", shape=box];
    }
    subgraph cluster_2 {
        label="commons-cli";
        color=yellow;
        style=dashed;
        Node_2 [label="commons-cli", shape=box];
    }
    subgraph cluster_3 {
        label="org.apache.maven";
        color=orange;
        style=dashed;
        Node_3 [label="maven-model", shape=box];
    }
    subgraph cluster_4 {
        label="org.fuin";
        color=purple;
        style=dashed;
        Node_4 [label="utils4j", shape=box];
    }
    Node_1 -> Node_0 [color=green,penwidth=1];
    Node_1 -> Node_3 [color=green,penwidth=1];
    Node_1 -> Node_4 [color=green,penwidth=1];
    Node_1 -> Node_2 [color=green,penwidth=1];
}
```
Which can be run through a 'dot' image process to produce an image such as that shown [here](https://github-camo.global.ssl.fastly.net/f0f947fe53944c3d3dc7cffb5b311c3d0f75dab4/687474703a2f2f692e696d6775722e636f6d2f625a38643933502e706e67 "Dependency Graph").

