package uk.me.thega;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;

import uk.me.thega.url.RepoURLReader;


import fr.loria.GraphViz;

public class Analyser {

	private static String[] colours = { "red", "blue", "green", "yellow", "orange", "purple", "maroon", "brown", "greenyellow", "olive" };

	private final Map<String, Map<String, List<String>>> depsByVersion = new HashMap<String, Map<String, List<String>>>();

	private final Map<String, List<String>> artifactsToCreate = new HashMap<String, List<String>>();

	private final List<String> repos;

	private final String restriction;

	private final String username;

	private final String password;

	public static void main(final String[] args) throws Exception {
		final AnalyserCLI cli = new AnalyserCLI();
		cli.parse(args);

		final String restriction = cli.getRestriction();
		final List<String> repos = cli.getRepoPaths();
		final String username = cli.getUsername();
		final String password = cli.getPassword();

		final Analyser analyser = new Analyser(repos, restriction, username, password);
		analyser.analyse();
	}

	static Map<String, String> createNodesForArtifacts(final Map<String, List<String>> artifactsToCreate) {
		final Map<String, String> nodeMap = new HashMap<String, String>();
		int i = 0;
		for (final String groupId : artifactsToCreate.keySet()) {
			for (final String artifactId : artifactsToCreate.get(groupId)) {
				nodeMap.put(groupId + ":" + artifactId, "Node_" + i++);
			}
		}
		return nodeMap;
	}

	public Analyser(final List<String> repos, final String restriction, final String username, final String password) {
		this.repos = repos;
		this.restriction = restriction;
		this.username = username;
		this.password = password;
	}

	public void analyse() throws Exception {
		System.out.println("Processing");

		final RepoURLReader pomReader = new RepoURLReader(username, password);
		final List<Model> poms = new ArrayList<Model>();

		for (final String repo : repos) {
			poms.addAll(pomReader.readPomsAt(repo, ""));
		}

		for (final Model pom : poms) {
			final String groupId = (pom.getGroupId() == null) ? pom.getParent().getGroupId() : pom.getGroupId();
			final String artifactId = pom.getArtifactId();

			final List<Dependency> dependencies = pom.getDependencies();
			for (final Dependency dependency : dependencies) {
				final String depGroupId = dependency.getGroupId();
				final String depArtifactId = dependency.getArtifactId();
				final String depVersion = dependency.getVersion();

				addDependencyBetweenAtrifacts(depGroupId, depArtifactId, depVersion, groupId, artifactId);
			}
		}

		System.out.println("Processed " + poms.size() + " modules");

		final Map<String, String> nodeMap = createNodesForArtifacts(artifactsToCreate);
		final Map<String, String> nodeColour = new HashMap<String, String>();

		final GraphViz gv = new GraphViz();
		gv.addln(gv.start_graph());
		gv.addln("size=\"1000,1000\";");

		int i = 0;

		for (final String groupId : artifactsToCreate.keySet()) {
			gv.addln("subgraph cluster_" + i++ + " {");
			gv.addln("label=\"" + groupId + "\";");
			gv.addln("color=" + colours[i] + ";");
			gv.addln("style=dashed;");
			for (final String artifactId : artifactsToCreate.get(groupId)) {
				final String artifact = groupId + ":" + artifactId;
				nodeColour.put(artifact, colours[i]);
				gv.addln(nodeMap.get(artifact) + " [label=\"" + artifactId + "\", shape=box];");
			}
			gv.addln("}");
		}

		for (final String dependency : depsByVersion.keySet()) {
			final Map<String, List<String>> versionUsers = depsByVersion.get(dependency);
			for (final String version : versionUsers.keySet()) {
				for (final String user : versionUsers.get(version)) {
					if (nodeMap.get(dependency) == null || nodeMap.get(user) == null) {
						System.out.println("BROKEN: " + user + " -> " + dependency);
					} else if (user.startsWith(restriction) || dependency.startsWith(restriction)) {
						gv.addln(nodeMap.get(user) + " -> " + nodeMap.get(dependency) + " [color=" + nodeColour.get(user) + "];");
					}
				}
			}
		}

		gv.addln(gv.end_graph());
		gv.addln();
		System.out.println(gv.getDotSource());
	}

	private void addDependencyBetweenAtrifacts(final String depGroupId, final String depArtifactId, final String depVersion, String groupId, String artifactId) {
		if (!depGroupId.startsWith(restriction)) {
			return;
		}

		// Create the UUID for this artifact
		final String refererUUID = groupId + ":" + artifactId;

		// Create the UUID for this dependency
		final String depUUID = depGroupId + ":" + depArtifactId;

		// Add the artifact to our list of artifacts to create
		addArtifactToList(groupId, artifactId);
		addArtifactToList(depGroupId, depArtifactId);

		if (!depsByVersion.containsKey(depUUID)) {
			depsByVersion.put(depUUID, new HashMap<String, List<String>>());
		}
		final Map<String, List<String>> artifactVersions = depsByVersion.get(depUUID);
		if (!artifactVersions.containsKey(depVersion)) {
			artifactVersions.put(depVersion, new ArrayList<String>());
		}
		final List<String> referencingArtifacts = artifactVersions.get(depVersion);
		referencingArtifacts.add(refererUUID);
	}

	private void addArtifactToList(final String groupId, final String artifactId) {
		if (!groupId.startsWith(restriction)) {
			return;
		}
		if (!artifactsToCreate.containsKey(groupId)) {
			artifactsToCreate.put(groupId, new ArrayList<String>());
		}
		final List<String> list = artifactsToCreate.get(groupId);
		if (!list.contains(artifactId)) {
			list.add(artifactId);
		}
	}
}
