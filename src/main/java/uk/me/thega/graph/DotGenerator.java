package uk.me.thega.graph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.loria.GraphViz;

/**
 * {@link GraphViz} wrapper for Analyser output
 *  
 * @author pwhittlesea
 *
 */
public class DotGenerator {

	/** The colours generator for nodes. */
	private final DotColours colours = new DotColours();

	/** The Id of the current cluster. */
	private int clusterNumber = 0;

	/** The Id of the current node. */
	private int nodeNumber = 0;

	/** The colours of each groupId. */
	private Map<String, String> groupIdColours = new HashMap<String, String>();

	/** The node number for a specific artifact. */
	private Map<String, String> nodeNumbersByArtifact = new HashMap<String, String>();

	/** Our current graph. */
	private GraphViz gv;

	/**
	 * Return the string that will represent a node on the Graphiz graph.
	 * 
	 * @param nodeId the nodeId of the node.
	 * @param label the label for the node.
	 * @return the string for the node.
	 */
	static final String nodeString(final String nodeId, final String label) {
		return "        " + nodeId + " [label=\"" + label + "\", shape=box];";
	}

	/**
	 * Return the string that will represent a link between
	 * two nodes on the Graphiz graph.
	 *
	 * @param from the node the link comes from.
	 * @param to the node the link goes to.
	 * @param colour the colour of the link.
	 * @param penwidth the weight of the link.
	 * @return the string for the link.
	 */
	static final String linkString(final String from, final String to, final String colour, final int penwidth) {
		return "    " + from + " -> " + to + " [color=" + colour + ",penwidth=" + penwidth + "];";
	}

	/**
	 * End the current graph.
	 */
	public String end() {
		gv.addln(gv.end_graph());
		gv.addln();

		return gv.getDotSource();
	}

	/**
	 * Return the node Id for a group and artifact.
	 * 
	 * @param groupId the groupId of the artifact.
	 * @param artifactId the artifactId of the artifact.
	 * @return the nodeId string.
	 */
	String getNodeIdForArtifact(final String groupId, final String artifactId) {
		final String artifactUUID = groupId + ":" + artifactId;
		if (!nodeNumbersByArtifact.containsKey(artifactUUID)) {
			nodeNumbersByArtifact.put(artifactUUID, "Node_" + nodeNumber++);
		}
		return nodeNumbersByArtifact.get(artifactUUID);
	}

	/**
	 * Add a link to the graph between two nodes of a certain colour
	 * and weight.
	 * 
	 * @param depGroupId the groupId of the dependency.
	 * @param depArtifactId the artifactId of the dependency.
	 * @param groupId the groupId of the dependee.
	 * @param artifactId the artifactId of the dependee.
	 * @param importance the importance (weight) of the link.
	 */
	public void linkNodesOnGraph(final String depGroupId, final String depArtifactId, final String groupId, final String artifactId, final int importance) {
		final String dependeeNodeId = getNodeIdForArtifact(groupId, artifactId);
		final String dependencyNodeId = getNodeIdForArtifact(depGroupId, depArtifactId);
		if (dependeeNodeId != null && dependencyNodeId != null) {
			gv.addln(linkString(dependeeNodeId, dependencyNodeId, groupIdColours.get(groupId), importance));
		}
	}

	/**
	 * Start a new graph.
	 * 
	 * @param width the width of the graph.
	 * @param height the height of the graph.
	 */
	public void start(final int width, final int height) {
		gv = new GraphViz();
		gv.addln(gv.start_graph());
		gv.addln("    size=\"" + width + "," + height + "\";");

		// Clean up
		clusterNumber = 0;
		nodeNumber = 0;
		groupIdColours.clear();
		nodeNumbersByArtifact.clear();
	}

	/**
	 * Write all of the artifacts to the graph.
	 * 
	 * @param cluster do we cluster by group?
	 * @param artifactsByGroupId the artifacts grouped by groupId
	 */
	public void writeNodesToGraph(final boolean cluster, final Map<String, List<String>> artifactsByGroupId) {
		for (final String groupId : artifactsByGroupId.keySet()) {
			final List<String> artifactIds = artifactsByGroupId.get(groupId);
			final String groupIdColour = colours.next();
			groupIdColours.put(groupId, groupIdColour);

			if (cluster) {
				gv.addln("    subgraph cluster_" + clusterNumber++ + " {");
				gv.addln("        label=\"" + groupId + "\";");
				gv.addln("        color=" + groupIdColour + ";");
				gv.addln("        style=dashed;");
			}

			for (final String artifactId : artifactIds) {
				final String nodeId = getNodeIdForArtifact(groupId, artifactId);
				gv.addln(nodeString(nodeId, artifactId));
			}

			if (cluster) {
				gv.addln("    }");
			}
		}
	}
}
