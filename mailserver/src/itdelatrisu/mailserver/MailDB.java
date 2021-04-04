// Info: File contains changes by Philipp Eichinger (@peichinger)

package itdelatrisu.mailserver;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

/**
 * Database connection manager.
 */
public class MailDB {
	/** Delimiter for URLs. */
	private static final char URL_DELIMITER = '\r';

	/** Maximum allowed length of a URL. */
	private static final int MAX_URL_LENGTH = 2048;

	/** Maximum allowed length of a domain name. */
	private static final int MAX_DOMAIN_LENGTH = 255;

	/** The data source. */
	private final BasicDataSource dataSource;

	/** The organization domains manager. */
	private final OrganizationDomains orgs;

	/** Represents a email user. */
	public class MailUser {
		private final int id, emailCount, emailCount2, leakCount, tpLeakCount;
		private final String email, email2, site, url, urlDomain, category;
		private final Date ts;

		/** Constructor. */
		public MailUser(
			int id, String email, String email2, String site, String url, String urlDomain, String category, Date ts,
			int emailCount, int emailCount2, int leakCount, int tpLeakCount
		) {
			this.id = id;
			this.email = email;
			this.email2 = email2;
			this.site = site;
			this.url = url;
			this.urlDomain = urlDomain;
			this.category = category;
			this.ts = ts;
			this.emailCount = emailCount;
			this.emailCount2 = emailCount2;
			this.leakCount = leakCount;
			this.tpLeakCount = tpLeakCount;
		}

		/** Returns the unique user ID. */
		public int getId() { return id; }

		/** Returns the unique email address. */
		public String getEmail() { return email; }

		/** PE: Returns the unique secondary email address. */
		public String getEmail2() { return email2; }

		/** Returns the registration site title. */
		public String getRegistrationSiteTitle() { return site; }

		/** Returns the registration site URL. */
		public String getRegistrationSiteUrl() { return url; }

		/** Returns the registration site domain. */
		public String getRegistrationSiteDomain() { return urlDomain; }

		/** PE: Returns the category of the registration site */
		public String getRegistrationSiteCategory() { return category; }

		/** Returns the registration date. */
		public Date getRegistrationDate() { return ts; }

		/** Returns the number of emails this user received (may be out of date). */
		public int getReceivedEmailCount() { return emailCount; }

		/** PE: Returns the number of secondary emails this user received (may be out of date). */
		public int getReceivedEmailCount2() { return emailCount2; }

		/** Returns the number of times the user's email address was leaked (may be out of date). */
		public int getLeakCount() { return leakCount; }

		/** Returns the number of times the user's email address was leaked to a third party (may be out of date). */
		public int getThirdPartyLeakCount() { return tpLeakCount; }
	}

	/** Represents a link group. */
	// PE: Not in use
	public class LinkGroup {
		private final int id;
		private final String senderDomain, senderAddress;
		private final int recipientId;
		private final String[] urls;

		/** Constructor. */
		public LinkGroup(int id, String senderDomain, String senderAddress, int recipientId, String[] urls) {
			this.id = id;
			this.senderDomain = senderDomain;
			this.senderAddress = senderAddress;
			this.recipientId = recipientId;
			this.urls = urls;
		}

		/** Returns the unique link group ID. */
		public int getId() { return id; }

		/** Returns the sender domain. */
		public String getSenderDomain() { return senderDomain; }

		/** Returns the sender email address. */
		public String getSenderAddress() { return senderAddress; }

		/** Returns the recipient's user ID. */
		public int getRecipientId() { return recipientId; }

		/** Returns the link URLs in this group. */
		public String[] getUrls() { return urls; }
	}

	/** Initializes the connection pool. */
	public MailDB(String driver, String url, String username, String password) {
		this.dataSource = new BasicDataSource();
		dataSource.setDriverClassName(driver);
		dataSource.setUrl(url);
		dataSource.setUsername(username);
		dataSource.setPassword(password);

		this.orgs = new OrganizationDomains();
	}

	/** Returns a database connection. */
	private Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	/** Truncates the given URL if it is too long. */
	private String truncateUrl(String url) {
		if (url.length() <= MAX_URL_LENGTH)
			return url;

		String marker = "[TRUNCATED]";
		return url.substring(0, MAX_URL_LENGTH - marker.length()) + marker;
	}

	/** Adds a email entry to the database. */
	public void addMailEntry(
		String into_table,
		String recipient,
		String sender,
		Date sentDate,
		String subject,
		String filename,
		String format
	) throws SQLException {
		if

		try (
			Connection connection = getConnection();
			PreparedStatement stmt = connection.prepareStatement(
				"INSERT INTO `?` VALUES(?, ?, ?, ?, ?, ?, ?)"
			);
			PreparedStatement stmtUpdate = connection.prepareStatement(
				"UPDATE `users` SET `?` = `?` + 1 WHERE `?` = ?"
			);
		) {
			stmt.setString(1, into_table);
			stmt.setString(2, recipient);
			stmt.setString(3, sender);
			stmt.setTimestamp(4, sentDate == null ? null : new Timestamp(sentDate.getTime()));
			stmt.setString(5, subject);
			stmt.setString(6, filename);
			stmt.setString(7, format);
			stmt.setString(8, "");
			stmt.executeUpdate();

			if(into_table.equals("inbox")){
				stmtUpdate.setString(1, "emails_received");
				stmtUpdate.setString(2, "emails_received");
				stmtUpdate.setString(3, "email");
			}else{
				stmtUpdate.setString(1, "emails_received2");
				stmtUpdate.setString(2, "emails_received2");
				stmtUpdate.setString(3, "email2");
			}
			stmtUpdate.setString(4, recipient);
			stmtUpdate.executeUpdate();
		}
	}

	/** Adds a redirect chain to the database. */
	// PE: Not in use
	public synchronized void addRedirects(
		Request req,
		String senderDomain,
		String senderAddress,
		int recipientId
	) throws SQLException {
		if (req.getRedirects().isEmpty())
			return;
		try (
			Connection connection = getConnection();
			PreparedStatement stmt = connection.prepareStatement(
				"INSERT INTO `redirects` VALUES(?, ?, ?, ?, ?, ?, ?, ?)"
			);
		) {
			String requestUrl = req.getURL().toString();
			List<URL> redirects = req.getRedirects();
			for (int i = 0; i < redirects.size(); i++) {
				stmt.setString(1, senderDomain);
				stmt.setString(2, senderAddress);
				stmt.setInt(3, recipientId);
				stmt.setString(4, truncateUrl(requestUrl));
				String redirectDomain;
				try {
					redirectDomain = Utils.getDomainName(redirects.get(i).toString());
					if (redirectDomain.length() > MAX_DOMAIN_LENGTH)
						redirectDomain = "";
				} catch (Exception e) {
					redirectDomain = "";
				}
				stmt.setString(5, redirectDomain);
				stmt.setString(6, redirectDomain.isEmpty() ? null : orgs.getOrganizationForDomain(redirectDomain));
				stmt.setString(7, truncateUrl(redirects.get(i).toString()));
				stmt.setInt(8, i + 1);
				stmt.executeUpdate();
			}
		}
	}

	/** Adds a URL containing an email address to the database. */
	public void addLeakedEmailAddress(
		String url,
		String type,
		String encoding,
		boolean isRedirect,
		boolean isIntentional,
		String senderDomain,
		String senderAddress,
		int recipientId
	) throws SQLException {
		try (
			Connection connection = getConnection();
			PreparedStatement stmt = connection.prepareStatement(
				"INSERT INTO `leaked_emails` VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
			);
			PreparedStatement stmtUpdate = connection.prepareStatement(
				"UPDATE `users` SET `leak_count` = `leak_count` + 1 WHERE `id` = ?"
			);
		) {
			stmt.setString(1, senderDomain);
			stmt.setString(2, senderAddress);
			stmt.setInt(3, recipientId);
			stmt.setString(4, encoding);
			stmt.setString(5, truncateUrl(url));
			String urlDomain;
			try {
				urlDomain = Utils.getDomainName(url);
				if (urlDomain.length() > MAX_DOMAIN_LENGTH)
					urlDomain = "";
			} catch (Exception e) {
				urlDomain = "";
			}
			stmt.setString(6, urlDomain);
			stmt.setString(7, urlDomain.isEmpty() ? null : orgs.getOrganizationForDomain(urlDomain));
			stmt.setString(8, type);
			stmt.setBoolean(9, isRedirect);
			stmt.setBoolean(10, isIntentional);
			stmt.executeUpdate();

			stmtUpdate.setInt(1, recipientId);
			stmtUpdate.executeUpdate();

			if (!urlDomain.isEmpty() && !senderDomain.equals(urlDomain)) {
				try (
					PreparedStatement stmtUpdateTp = connection.prepareStatement(
						"UPDATE `users` SET `tp_leak_count` = `tp_leak_count` + 1 WHERE `id` = ?"
					);
				) {
					stmtUpdateTp.setInt(1, recipientId);
					stmtUpdateTp.executeUpdate();
				}
			}
		}
	}

	/** Adds a email user to the database, and returns false if the user already existed. */
	public boolean addMailUser(String email, String email2, String site, String url, String category) throws SQLException {
		try (
			Connection connection = getConnection();
			PreparedStatement stmt = connection.prepareStatement(
				"INSERT IGNORE INTO `users` (`email`, `email2`, `register_site`, `register_url`, `register_domain`, `register_category`) VALUES(?, ?, ?, ?, ?, ?)"
			);
		) {
			stmt.setString(1, email);
			stmt.setString(2, email2)
			stmt.setString(3, site);
			stmt.setString(4, truncateUrl(url));
			try {
				stmt.setString(5, Utils.getDomainName(url));
			} catch (Exception e) {
				stmt.setString(5, "");
			}
			stmt.setString(6, category);
			int rows = stmt.executeUpdate();
			return rows > 0;
		}
	}

	/** Returns whether the given user exists. */
	public boolean userExists(String email) throws SQLException {
		try (
			Connection connection = getConnection();
			PreparedStatement stmt = connection.prepareStatement(
				"SELECT EXISTS(SELECT 1 FROM `users` WHERE `email` = ? OR `email2` = ?)"
			);
		) {
			stmt.setString(1, email);
			stmt.setString(2, email);
			stmt.executeQuery();
			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next() ? rs.getBoolean(1) : false;
			}
		}
	}

	/** Returns user data for the given email address, or null if it does not exist. */
	public MailUser getUserInfo(String email) throws SQLException {
		try (
			Connection connection = getConnection();
			PreparedStatement stmt = connection.prepareStatement(
				"SELECT `id`, `email`, `email2`, `register_site`, `register_url`, `register_domain`, `register_category`, `register_time`, `emails_received`, `emails_received2`, `leak_count`, `tp_leak_count` FROM `users` WHERE `email` = ? OR `email2` = ?"
			);
		) {
			stmt.setString(1, email);
			stmt.setString(2, email);
			stmt.executeQuery();
			try (ResultSet rs = stmt.executeQuery()) {
				return (!rs.next()) ? null :
					new MailUser(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), rs.getTimestamp(8), rs.getInt(9), rs.getInt(10), rs.getInt(11), rs.getInt(12));
			}
		}
	}

	/** Returns user data for the given user ID, or null if it does not exist. */
	public MailUser getUserInfo(int id) throws SQLException {
		try (
			Connection connection = getConnection();
			PreparedStatement stmt = connection.prepareStatement(
				"SELECT `email`, `email2`, `register_site`, `register_url`, `register_domain`, `register_category`, `register_time`, `emails_received`, `emails_received2`, `leak_count`, `tp_leak_count` FROM `users` WHERE `id` = ?"
			);
		) {
			stmt.setInt(1, id);
			stmt.executeQuery();
			try (ResultSet rs = stmt.executeQuery()) {
				return (!rs.next()) ? null :
					new MailUser(id, rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getTimestamp(7), rs.getInt(8), rs.getInt(9), rs.getInt(10), rs.getInt(11));
			}
		}
	}

	/** Returns a list of all user data. */
	public List<MailUser> getUsers() throws SQLException {
		try (
			Connection connection = getConnection();
			Statement stmt = connection.createStatement();
		) {
			String sql = "SELECT `id`, `email`, `email2`, `register_site`, `register_url`, `register_domain`, `register_category`, `register_time`, `emails_received`, `emails_received2`, `leak_count`, `tp_leak_count` FROM `users`";
			List<MailUser> users = new ArrayList<MailUser>();
			try (ResultSet rs = stmt.executeQuery(sql)) {
				while (rs.next())
					users.add(new MailUser(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), rs.getTimestamp(8), rs.getInt(9), rs.getInt(10), rs.getInt(11), rs.getInt(12)));
			}
			return users;
		}
	}

	/** Adds a group of links to the database. */
	// PE: Not in use
	public void addLinkGroup(
		List<String> urls,
		String senderDomain,
		String senderAddress,
		int recipientId
	) throws SQLException {
		try (
			Connection connection = getConnection();
			PreparedStatement stmt = connection.prepareStatement(
				"INSERT INTO `link_groups` (`sender_domain`, `sender_address`, `recipient_id`, `urls`) VALUES(?, ?, ?, ?)"
			);
		) {
			stmt.setString(1, senderDomain);
			stmt.setString(2, senderAddress);
			stmt.setInt(3, recipientId);
			stmt.setString(4, String.join(Character.toString(URL_DELIMITER), urls));
			stmt.executeUpdate();
		}
	}

	/** Retrieves a random group of links from the database, or null if none exists. */
	// PE: Not in use
	public LinkGroup getLinkGroup() throws SQLException {
		try (
			Connection connection = getConnection();
			Statement stmt = connection.createStatement();
		) {
			String sql = "SELECT `id`, `sender_domain`, `sender_address`, `recipient_id`, `urls` FROM `link_groups` ORDER BY RAND() LIMIT 1";
			try (ResultSet rs = stmt.executeQuery(sql)) {
				if (!rs.next())
					return null;
				String[] urls = rs.getString(5).split(Character.toString(URL_DELIMITER));
				return new LinkGroup(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4), urls);
			}
		}
	}

	/** Retrieves link group data for the given ID from the database, or null if it does not exist. */
	// PE: Not in use
	public LinkGroup getLinkGroup(int id) throws SQLException {
		try (
			Connection connection = getConnection();
			PreparedStatement stmt = connection.prepareStatement(
				"SELECT `sender_domain`, `sender_address`, `recipient_id`, `urls` FROM `link_groups` WHERE `id` = ?"
			);
		) {
			stmt.setInt(1, id);
			stmt.executeQuery();
			try (ResultSet rs = stmt.executeQuery()) {
				if (!rs.next())
					return null;
				String[] urls = rs.getString(4).split(Character.toString(URL_DELIMITER));
				return new LinkGroup(id, rs.getString(1), rs.getString(2), rs.getInt(3), urls);
			}
		}
	}

	/** Removes link group data for the given ID from the database. */
	// PE: Not in use
	public void removeLinkGroup(int id) throws SQLException {
		try (
			Connection connection = getConnection();
			PreparedStatement stmt = connection.prepareStatement(
				"DELETE FROM `link_groups` WHERE `id` = ?"
			);
		) {
			stmt.setInt(1, id);
			stmt.executeUpdate();
		}
	}
}
