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
INSERT INTO users (email, register_site, register_url, register_domain) values('test@mail.eichinger-edv.at', 'test', 'test', 'test');

-- Delete tables:
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS inbox;
```

## Windos powershell:
```powershell
# Register user via the Java web server:
curl -X POST 10.0.0.11:8080/register -d "site=orf.at&url=orf.at/registernewuser"

# Determine which ports of the server are open:
nmap 10.0.0.11
```