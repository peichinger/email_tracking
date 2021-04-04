// Info: File contains changes by Philipp Eichinger (@peichinger)

package itdelatrisu.mailserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Email address generator.
 */
public class EmailAddressGenerator {
	private static final Logger logger = LoggerFactory.getLogger(EmailAddressGenerator.class);

	/** Name lists. */
	private final List<String> firstNames, surnames;

	/** Initializes the generator. */
	// PE: first_names_at.txt - The 60 most common female and male first names of newborns in Austria in 2019
	// PE: surnames_at.txt - The 50 most common family names in Austria
	public EmailAddressGenerator() {
		this.firstNames = loadNames("first_names_at.txt");
		this.surnames = loadNames("surnames_at.txt");
	}

	/** Loads names line-by-line from the given resource file. */
	private List<String> loadNames(String res) {
		ArrayList<String> list = new ArrayList<>();
		try (
			InputStream in = getClass().getResourceAsStream("/" + res);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (!line.isEmpty())
					list.add(line.toLowerCase().trim());
			}
		} catch (IOException e) {
			logger.error("Failed to load name list.", e);
		}
		list.trimToSize();
		return list;
	}

	/** Returns a random element from a list. */
	private String random(List<String> list) {
		return list.get((int) (Math.random() * list.size()));
	}

	/** Generates an email address with the given domain name. */
	public String generate(String domain) {
		StringBuilder sb = new StringBuilder();
//		sb.append(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
		sb.append(random(firstNames));
		sb.append(".");
		sb.append(random(surnames));
		sb.append((int) (Math.random() * 1000));
		sb.append('@');
		sb.append(domain);
		return sb.toString();
	}
}
