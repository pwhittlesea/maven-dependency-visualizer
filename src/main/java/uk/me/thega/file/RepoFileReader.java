package uk.me.thega.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class RepoFileReader {

	public static List<String> getReposFromFile(final String fileName) throws IOException {
		final List<String> repos = new ArrayList<String>();
		InputStream content = null;
		BufferedReader in = null;
		try {
			final File file = new File(fileName);
			if (!file.exists()) {
				throw new IOException("File does not exist.");
			}

			content = new FileInputStream(file);
			in = new BufferedReader(new InputStreamReader(content));

			String line;
			while ((line = in.readLine()) != null) {
				repos.add(line.trim());
			}
			return repos;
		} catch (final IOException e) {
			throw new IOException(e);
		} finally {
			if (in != null) {
				in.close();
			}
			if (content != null) {
				content.close();
			}
		}
	}
}
