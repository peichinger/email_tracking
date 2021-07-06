// Info: File contains changes by Philipp Eichinger (@peichinger)

package itdelatrisu.mailserver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Date;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Storage handler for incoming email.
 */
public class MailStorage {
	private static final Logger logger = LoggerFactory.getLogger(MailStorage.class);

	/** Default root mail directory. */
	private static final File DEFAULT_MAIL_DIR = new File("mail");

	/** Default secondary root mail directory. */
	private static final File DEFAULT_MAIL_DIR2 = new File("mail2");

	/** The database instance. */
	private final MailDB db;

	/** The root mail directory. */
	private final File mailDir;

	/** The secondary root mail directory. */
	private final File mailDir2;

	/** Initializes the storage module. */
	public MailStorage(MailDB db) {
		this(db, DEFAULT_MAIL_DIR, DEFAULT_MAIL_DIR2);
	}

	/** Initializes the storage module. */
	public MailStorage(MailDB db, File rootDir, File rootDir2) {
		this.db = db;
		this.mailDir = rootDir;
		this.mailDir2 = rootDir2;
		if (!mailDir.isDirectory() && !mailDir.mkdirs())
			logger.error("Failed to create root mail directory '{}'.", mailDir.getAbsolutePath());
		if (!mailDir2.isDirectory() && !mailDir2.mkdirs())
			logger.error("Failed to create secondary root mail directory '{}'.", mailDir2.getAbsolutePath());
	}

	/** Stores the message. */
	public boolean store(String from, MailDB.MailUser user, String data, boolean recipient_is_email1) {
		boolean success = false;
		File mailDirX;
		String email;
		
		// PE: Check whether the recipient is a secondary email address or not
		// PE: -> Emails to a secondary email address are stored in a different directory and table 
		if(recipient_is_email1){
			mailDirX = mailDir;
			email = user.getEmail();
		} else{
			mailDirX = mailDir2;
			email = user.getEmail2();
		}

		// {root_mail_dir}/{email}/{timestamp}.eml
		File dir = new File(mailDirX, Utils.cleanFileName(email, '_'));
		if (!dir.isDirectory() && !dir.mkdirs()) {
			logger.error("Failed to create mail directory '{}'.", dir.getAbsolutePath());
			dir = mailDirX;
		}
		String filename = String.format("%d.eml", System.currentTimeMillis());
		File file = new File(dir, filename);

		// write contents to file
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
			writer.write(stripAttachments(data));
			success = true;
		} catch (IOException e) {
			logger.error("Failed to write email to disk.", e);
		}

		// write email entry into database
		String subject = null;
		Date sentDate = null;
		String html = null;
		String format = null;
		try {
			MimeMessage message = Utils.toMimeMessage(data);
			subject = message.getSubject();
			sentDate = message.getSentDate();

			//PE: Check if the email format is html?
			html = Utils.getHtmlFromMessage(message);
			if (html == null){
				format = "other";
			}else {
				format = "html";
			}
		} catch (MessagingException | IOException e) {
			logger.error("Failed to parse message.", e);
			success = false;
		}
		try {
			if(recipient_is_email1){
				db.addMailEntry(user.getEmail(), from, sentDate, subject, file.getName(), format);
			} else{
				db.addMailEntry2(user.getEmail2(), from, sentDate, subject, file.getName(), format);
			}
		} catch (SQLException e) {
			logger.error("Failed to log message to database.", e);
			success = false;
		}
		return success;
	}

	/** Strips attachments in the given message. */
	private String stripAttachments(String data) {
		try {
			// parse MIME message
			MimeMessage message = Utils.toMimeMessage(data);
			Object content = message.getContent();
			if (!(content instanceof Multipart))
				return data;  // not a multipart message

			// strip attachments
			Multipart multipart = (Multipart) content;
			if (stripAttachments(multipart)) {
				message.setContent(multipart);
				message.saveChanges();
				return Utils.messageToString(message);
			} else
				return data;  // content unmodified
		} catch (MessagingException | IOException e) {
			logger.error("Error while stripping attachments.", e);
			return data;
		}
	}

	/**
	 * Recursively strips attachments from a multipart message,
	 * and returns whether the message was modified.
	 */
	private boolean stripAttachments(Multipart multipart) throws MessagingException, IOException {
		boolean modified = false;
		for (int i = multipart.getCount() - 1; i >= 0; i--) {
			Part part = multipart.getBodyPart(i);
			String contentType = part.getContentType();
			if (contentType.startsWith("multipart/")) {
				if (stripAttachments((Multipart) part.getContent()))
					modified = true;
			} else if (discardMimeType(contentType)) {
				modified = true;
				multipart.removeBodyPart(i);
			}
		}
		return modified;
	}

	/** Returns whether to discard content with this MIME type. */
	private boolean discardMimeType(String contentType) {
		return !contentType.startsWith("text/");
	}
}
