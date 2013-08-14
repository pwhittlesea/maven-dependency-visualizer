package uk.me.thega.url;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Model;
import org.codehaus.plexus.util.Base64;
import org.fuin.maven.MavenPomReader;

/**
 * Class to read the URLs from remote locations.
 * 
 * @author pwhittlesea
 *
 */
public class RepoURLReader {

	/** The temporary file to cache the pom.xml to. */
	private static final File outputFile;

	/** The name of the file to search for in each folder. */
	private static final String pomName = "pom.xml";

	/** The HTTP username for basic auth. */
	private final String username;

	/** The HTTP password for basic auth. */
	private final String password;
	
	static {
		String tempPath = System.getProperty("java.io.tmpdir");
		// a fix to handle the crazy path the Mac JVM returns
		if (tempPath.startsWith("/var/folders/")) {
			tempPath = "/tmp/";
		}
		final File repositoryDir = new File(tempPath + "/maven");
		if (!repositoryDir.exists()) {
			repositoryDir.mkdirs();
		}
		outputFile = new File(repositoryDir + "/temp.pom");
	}

	/**
	 * Read a file at a URL to a specified file.
	 * 
	 * @param url the URL to read.
	 * @param username the basic HTTP user name.
	 * @param password the basic HTTP password.
	 * @throws IOException if reading/writing the file fails.
	 */
	static void readGitUrlToFile(final URL url, final String username, final String password) throws IOException {
		final URLConnection uc = url.openConnection();
		
		if (username != null && password != null) {
			final String passwdstring = username + ":" + password;
			final String encoding = new String(Base64.encodeBase64(passwdstring.getBytes()));
			uc.setRequestProperty("Authorization", "Basic " + encoding);
		}
	
		final InputStream content = (InputStream) uc.getInputStream();
		final BufferedReader in = new BufferedReader(new InputStreamReader(content));
	
		List<String> lines = new ArrayList<String>();
		String line;
		while ((line = in.readLine()) != null) {
			lines.add(line);
		}
	
		in.close();
		
		final OutputStream os = new FileOutputStream(outputFile);
		for (final String p : lines) {
			os.write(p.getBytes());
			os.write('\n');
		}
		os.close();
	}

	/**
	 * Default constructor.
	 * 
	 * @param username the basic HTTP user name.
	 * @param password the basic HTTP password.
	 */
	public RepoURLReader(final String username, final String password) {
		this.username = username;
		this.password = password;
	}

	/**
	 * Read the pom (and child modules) at the specified location.
	 * 
	 * @param repo the repo to read from
	 * @return the list of Poms at the location
	 * @throws MalformedURLException if the repo specified cannot be loaded.
	 */
	public List<Model> readPomsAt(final String repo) throws MalformedURLException {
		final List<Model> modules = new ArrayList<Model>();
		recursivelyReadPomsAt(modules, repo, "");
		return modules;
	}

	/**
	 * Read the Pom (and child modules) at the specified location 
	 * and sub-directory
	 * 
	 * @param modules the list of already found modules.
	 * @param repo the repo to search.
	 * @param subDir the sub-directory to search.
	 * @throws MalformedURLException if the location cannot be loaded.
	 */
	private void recursivelyReadPomsAt(List<Model> modules, final String repo, final String subDir) throws MalformedURLException {
		final URL url = new URL(repo + subDir + pomName);

		try {
			RepoURLReader.readGitUrlToFile(url, username, password);
		} catch (Exception e) {
			return;
		}

		// Read the model for an artifact and a given version
		final Model model = MavenPomReader.readModelFromFile(outputFile, null);
		modules.add(model);

		// Find all children
		final List<String> subModules = model.getModules();
		for (final String subModule : subModules) {
			final String newPath = subDir + subModule + "/";
			recursivelyReadPomsAt(modules, repo, newPath);
		}
	}

}
