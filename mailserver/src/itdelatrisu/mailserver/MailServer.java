// Info: File contains changes by Philipp Eichinger (@peichinger)

package itdelatrisu.mailserver;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.helper.SimpleMessageListener;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;

/**
 * Main SMTP server class.
 */
public class MailServer extends SMTPServer {
	private static final Logger logger = LoggerFactory.getLogger(MailServer.class);

	/** SMTP message listener. */
	private static class MessageListener implements SimpleMessageListener {
		private final MailHandler handler;
		public MessageListener(MailDB db) { handler = new MailHandler(db); }

		@Override
		public boolean accept(String from, String recipient) {
			boolean help = handler.accept(from, recipient);
			
			if (help){
				logger.info("ACCEPT: {} -> {}", from, recipient);
			} else{
				logger.info("REJECT: {} -> {}", from, recipient);
			}
			return help;
		}

		@Override
		public void deliver(String from, String recipient, InputStream data)
			throws TooMuchDataException, IOException {
			String content = Utils.streamToString(data);
			//logger.info("Try to DELIVER: {} -> {}", from, recipient); //PE: This log entry is created by the MailHandler instead
			handler.handleMessage(from, recipient, content);
		}
	}

	/** Creates the SMTP server. */
	public MailServer(MailDB db) {
		super(new SimpleMessageListenerAdapter(new MessageListener(db)));
		setSystemProperties();
	}

	/** Sets system properties. */
	private void setSystemProperties() {
		// allow multipart messages with no body parts
		System.setProperty("mail.mime.multipart.allowempty", "true");

		// disable SNI support
		System.setProperty("jsse.enableSNIExtension", "false");
	}
}
