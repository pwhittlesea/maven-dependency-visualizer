package uk.me.thega.url;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Model;
import org.codehaus.plexus.util.Base64;
import org.fuin.maven.MavenPomReader;

public class RepoURLReader {

	private static final File outputFile;

	private static final String pomName = "pom.xml";

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

	private final String username;

	private final String password;
	
	public static void readGitUrlToFile(final URL url, final String username, final String password, final File outputFile) throws Exception {
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

	public RepoURLReader(final String username, final String password) {
		this.username = username;
		this.password = password;
	}

	public List<Model> readPomsAt(final String repo, final String subDir) throws Exception {
		final List<Model> modules = new ArrayList<Model>();
		recursivelyReadPomsAt(modules, repo, subDir);
		return modules;
	}

	private void recursivelyReadPomsAt(List<Model> modules, final String repo, final String subDir) throws Exception {
		final URL url = new URL(repo + pomName);

		try {
			RepoURLReader.readGitUrlToFile(url, username, password, outputFile);
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
