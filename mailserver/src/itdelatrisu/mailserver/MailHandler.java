// Info: File contains changes by Philipp Eichinger (@peichinger)

package itdelatrisu.mailserver;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for incoming email.
 */
public class MailHandler {
	private static final Logger logger = LoggerFactory.getLogger(MailHandler.class);

	/** The database instance. */
	private final MailDB db;

	/** The storage module instance. */
	private final MailStorage storage;

	/** The analyzer module instance. */
	private final MailAnalyzer analyzer;

	/** Creates the mail handler. */
	public MailHandler(MailDB db) {
		this.db = db;
		this.storage = new MailStorage(db);
		this.analyzer = new MailAnalyzer(db);
	}

	/** Returns whether to accept or reject this message. */
	public boolean accept(String from, String recipient) {
		// reject if email address not in database
		try {
			return db.userExists(recipient);
		} catch (SQLException e) {
			logger.error("Failed to query database.", e);
		}
		return true;
	}

	/** Handles the message. */
	public void handleMessage(String from, String recipient, String data) {
		boolean success = false;
		boolean recipient_is_email1 = false;

		// get user info
		MailDB.MailUser user;
		try {
			user = db.getUserInfo(recipient);
		} catch (SQLException e) {
			logger.error("Failed to query database.", e);
			return;
		}
		if (user == null) {
			logger.error("No user entry for email '{}'.", recipient);
			return;
		}

		// PE: Check whether the recipient is a secondary email address or not
		// PE: -> Emails to a secondary email address are stored differently and are not analyzed
		if (user.getEmail().equals(recipient)){
			recipient_is_email1 = true;
		}

		// store email on disk
		success = storage.store(from, user, data, recipient, recipient_is_email1);
		if(success){
			logger.info("Mail saved successfully ({} -> {})", from, recipient);
		}

		// PE: Analyze email (emails to secondary email addresses are not analyzed)
		if(recipient_is_email1){
			analyzer.analyze(from, user, data);
		}
	}
}
