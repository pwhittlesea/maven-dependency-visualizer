package uk.me.thega;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;

import uk.me.thega.file.RepoFileReader;
import uk.me.thega.graph.DotGenerator;
import uk.me.thega.url.RepoURLReader;

/**
 * Main class for running the maven-dependency analyser.
 * <br>
 * Takes arguments for:
 * <ul>
 * <li>The file to read the repo paths from</li>
 * <li>The restriction upon the artifacts mapped</li>
 * <li>The username if the repos are behind basic HTTP authentication</li>
 * <li>The password if the repos are behind basic HTTP authentication</li>
 * </ul>
 * 
 * @author pwhittlesea
 *
 */
public class Analyser {

	/** Collection to store the links between versions of dependencies and dependees. */
	private final Map<String, Map<String, List<String>>> dependedArtifacts = new HashMap<String, Map<String, List<String>>>();

	/** The list of artifacts by groupId. */
	private final Map<String, List<String>> artifactsByGroupId = new HashMap<String, List<String>>();

	/** The repos to search. */
	private final List<String> repos;

	/** The restriction upon links printed. */
	private final String restriction;

	/** The HTTP auth username. */
	private final String username;

	/** The HTTP auth password. */
	private final String password;

	/**
	 * The main method.
	 * 
	 * @param args the command line arguments
	 * @throws Exception if analysis fails
	 */
	public static void main(final String[] args) throws Exception {
		final AnalyserCLI cli = new AnalyserCLI();
		cli.parse(args);

		final String restriction = cli.getRestriction();
		final List<String> repos = RepoFileReader.getReposFromFile(cli.getFile());
		final String username = cli.getUsername();
		final String password = cli.getPassword();

		final Analyser analyser = new Analyser(repos, restriction, username, password);
		analyser.analyse();
	}

	/**
	 * Default constructor.
	 * 
	 * @param repos the list of repos to search
	 * @param restriction the restriction on created nodes
	 * @param username the HTTP auth username
	 * @param password the HTTP auth password
	 */
	public Analyser(final List<String> repos, final String restriction, final String username, final String password) {
		this.repos = repos;
		this.restriction = restriction;
		this.username = username;
		this.password = password;
	}

	/**
	 * Analyse the repos and generate the graph
	 * 
	 * @throws Exception if analysis fails
	 */
	public void analyse() throws Exception {
		System.out.println("Processing");

		final RepoURLReader pomReader = new RepoURLReader(username, password);

		// Fetch the models at the repo location
		final List<Model> poms = new ArrayList<Model>();
		for (final String repo : repos) {
			final List<Model> foundPoms = pomReader.readPomsAt(repo);
			poms.addAll(foundPoms);
		}

		for (final Model pom : poms) {
			final String groupId = (pom.getGroupId() == null) ? pom.getParent().getGroupId() : pom.getGroupId();
			final String artifactId = pom.getArtifactId();

			final List<Dependency> dependencies = pom.getDependencies();
			for (final Dependency dependency : dependencies) {
				addDependencyBetweenAtrifacts(dependency, groupId, artifactId);
			}
		}

		System.out.println("Processed " + poms.size() + " modules");

		final DotGenerator graphGenerator = new DotGenerator();
		graphGenerator.start(1000, 1000);
		
		// Write all the artifacts to the graph, this does not include links
		graphGenerator.writeNodesToGraph(true, artifactsByGroupId);

		// Get the list of dependencies seen
		for (final String dependency : dependedArtifacts.keySet()) {
			final String[] dependencyComponents = dependency.split(":");
			final String dependencyGroupId = dependencyComponents[0];
			final String dependencyArtifactId = dependencyComponents[1];

			// Get the list of dependency versions
			final Map<String, List<String>> dependedVersions = dependedArtifacts.get(dependency);
			for (final String dependencyVersion : dependedVersions.keySet()) {
				// TODO Figure out which is the latest version of a Dependency
				final boolean outOfDate = false;
				final int importance = (outOfDate) ? 10 : 1;
				// Iterate over the modules that depend upon our current dependency version 
				for (final String dependee : dependedVersions.get(dependencyVersion)) {
					// Get the groupId and artifactId from our components
					final String[] dependeeComponents = dependee.split(":");
					final String dependeeGroupId = dependeeComponents[0];
					final String dependeeArtifactId = dependeeComponents[1];
					if (dependee.startsWith(restriction) || dependency.startsWith(restriction)) {
						graphGenerator.linkNodesOnGraph(dependencyGroupId, dependencyArtifactId, dependeeGroupId, dependeeArtifactId, importance);
					}
				}
			}
		}
		System.out.println(graphGenerator.end());
	}

	/**
	 * Process incoming dependencies and add them to the internal storage.
	 *  
	 * @param dependency the dependency
	 * @param groupId the referencing groupId
	 * @param artifactId the referencing artifactId
	 */
	private void addDependencyBetweenAtrifacts(final Dependency dependency, String groupId, String artifactId) {
		final String depGroupId = dependency.getGroupId();
		final String depArtifactId = dependency.getArtifactId();
		final String depVersion = dependency.getVersion();
		
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

		if (!dependedArtifacts.containsKey(depUUID)) {
			dependedArtifacts.put(depUUID, new HashMap<String, List<String>>());
		}
		final Map<String, List<String>> artifactVersions = dependedArtifacts.get(depUUID);
		if (!artifactVersions.containsKey(depVersion)) {
			artifactVersions.put(depVersion, new ArrayList<String>());
		}
		final List<String> referencingArtifacts = artifactVersions.get(depVersion);
		referencingArtifacts.add(refererUUID);
	}

	/**
	 * Add an artifact to the list of artifacts stored by groupId.
	 * 
	 * @param groupId the groupId
	 * @param artifactId the artifactId
	 */
	private void addArtifactToList(final String groupId, final String artifactId) {
		if (!groupId.startsWith(restriction)) {
			return;
		}
		if (!artifactsByGroupId.containsKey(groupId)) {
			artifactsByGroupId.put(groupId, new ArrayList<String>());
		}
		final List<String> list = artifactsByGroupId.get(groupId);
		if (!list.contains(artifactId)) {
			list.add(artifactId);
		}
	}
}
