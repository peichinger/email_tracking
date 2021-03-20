// Info: File contains changes by Philipp Eichinger (@peichinger)

package itdelatrisu.mailserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Launcher class.
 */
public class Launcher {
	private static final Logger logger = LoggerFactory.getLogger(Launcher.class);
	private static final String DOMAIN_NAME = "eichinger-edv.at"; //"lorveskel.me";

	public static void main(String[] args) {
		String domain = DOMAIN_NAME;
		logger.info("Initializing for domain [{}]...", domain);

		// get mail database instance
		MailDB db = new MailDB(
			//"com.mysql.jdbc.Driver",
			"org.mariadb.jdbc.Driver",
			//"jdbc:mysql://localhost:3306/mail",
			"jdbc:mariadb://localhost:3306/mail",
			"mailserver",
			"S6TTAykTfAEMJjqN"  //PE: Change PW in the productive environment
		);

		// start mail server
		MailServer mailServer = new MailServer(db);
		mailServer.start();
		logger.info("Mail server running on port {}.", mailServer.getPort());

		// start web server
		WebServer webServer = new WebServer(db, domain);
		webServer.start();
		logger.info("Web server running on port {}.", webServer.getPort());
	}
}
