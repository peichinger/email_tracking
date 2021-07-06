**Info**: File created by Philipp Eichinger (@peichinger)

# Cheat Sheet
**Cheat sheet with various frequently used commands:**

## Java:
```bash
    # Create project (localtion /email_tracking/mailserver):
    sudo mvn clean package

    # Start mail server (localtion /email_tracking/mailserver):
    sudo java -jar target/mailserver.jar
```

## Ubuntu:
```bash
# Kill process:
ps aux | grep java
kill #*psid*

# ufw firewall:
sudo ufw allow 25
sudo ufw allow 8080

# Determine which ports are open locally (but local firewall can block them):
netstat -tulpn
```

## Maria DB:
```sql
-- Login:
sudo mysql -u root -p
-- Enter PW
use mail
-- Logout:
exit

-- creat database:
source ./sql-files/db.sql;

-- creat tables:
source ./sql-files/main.sql;

-- Query all email addresses:
SELECT * from users;

-- Insert test email address:
INSERT INTO users (email, email2, register_site, register_url, register_domain) values('o.test@eichinger-edv.at', 'o2.test@eichinger-edv.at', 'test', 'test', 'test');

UPDATE users SET register_site = 'Zara Home' WHERE register_site = 'Zara';

Select * from users where email = '';

-- Delete email:
DELETE FROM inbox WHERE filename = '1617914660361.eml';
Update users SET emails_received = 1 WHERE email = 'm.fabian.boehm2@eichinger-edv.at';

DELETE FROM inbox2 WHERE filename = '1617914660361.eml';
Update users SET emails_received2 = 1 WHERE email = 'f.elena.huber94@eichinger-edv.at';

-- Update user:
Update users SET register_domain = 'mariazell-info.at' WHERE email = 'f.oliver.baumgartner846@eichinger-edv.at';
Update user SET register_url = 'https://newsletter.meinbezirk.at/newsletter/index.php?pool=Burgenland' where email = 'm.leonie.wagner438@eichinger-edv.at';

-- Delete tables:
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS inbox;
DROP TABLE IF EXISTS inbox2;
DROP TABLE IF EXISTS leaked_emails;

-- Delete database:
DROP DATABASE mail;

-- Delete user:
DROP USER 'mailserver'@'localhost';

-- Delete wrong user record:
DELETE FROM users WHERE email = 'm.annika.lang897@eichinger-edv.at';
DELETE FROM inbox WHERE recipient = 'm.annika.lang897@eichinger-edv.at';
DELETE FROM inbox2 WHERE recipient = 'm2.annika.lang897@eichinger-edv.at';
```

## Export mail DB as SQLite DB:
**Link Stackoverflow**: https://stackoverflow.com/questions/5164033/export-a-mysql-database-to-sqlite-database
**Link GIT Project**: https://github.com/dumblob/mysql2sqlite
```bash
cd /home/lea68/EmailTracking/git/mysql2sqlite
sudo mysqldump --skip-extended-insert --compact mail > /home/lea68/EmailTracking/maildb.sql
./mysql2sqlite /home/lea68/EmailTracking/maildb.sql | sqlite3 mailsqlite.db
```

## Windos powershell:
```powershell
# Register user via the Java web server:
curl -X POST 10.0.0.11:8080/register -d "site=orf.at&url=orf.at/registernewuser&category=medien"

# Determine which ports of the server are open:
nmap 10.0.0.11
```

## GIT
```powershell
# Upload changes to github (create local backup bevor):
git add #file name or *
git commit -m "message"
git push https://github.com/peichinger/email_tracking.git mailserver
```

```bash
# Dowload to server (create local backup bevor):
sudo rm -rf email_tracking/
git clone https://github.com/peichinger/email_tracking.git

# copy
cp #[OPTIONEN] QUELLE ZIEL
```

## Run Java Programm as Service:
**Link**: https://dzone.com/articles/run-your-java-application-as-a-service-on-ubuntu
**Link**: https://medium.com/@sulmansarwar/run-your-java-application-as-a-service-on-ubuntu-544531bd6102

```bash
sudo systemctl daemon-reload
sudo systemctl enable my-mailserver.service
sudo systemctl start my-mailserver.service
sudo systemctl status my-mailserver.service
sudo journalctl -f -n 1000 -u my-mailserver.service
```