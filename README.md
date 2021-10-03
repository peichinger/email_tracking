# Information about this project

## In German:

Bei diesem Repository handelt es sich um einen „fork“ eines Projekts von Herrn Steven Englehardt (@englehardt) welches dieser im Jahr 2018 im Zuge seiner Arbeit „I never signed up for this! Privacy implications of email tracking.“ erstellt hat. Ziel seiner Arbeit war es die Auswirkungen von E-Mail-Tracking auf die Privatsphäre zu untersuchen.

Für meine Bachelorarbeit an der Fachhochschule St. Pölten (Österreich) führe ich eine ähnliche Untersuchung im Deutschsprachigen-Raum durch und baue dabei teilweise auf der Arbeit bzw. dem Projekt von Herrn Englehardt auf.

Dieses Projekt enthält diverse Änderungen im Ordner „mailserver“. Ein Überblick über die Änderungen ist in der README.md Datei im Ordner „mailserver“ zu finden.

Das Projekt hat Anfang Februar 2021 gestartet und wurde im September 2021 abgeschlossen. 

## In English:

This repository is a fork of a project by Mr. Steven Englehardt (@englehardt) which he did in 2018 as part of his research paper “I never signed up for this! Privacy implications of email tracking." The aim of his project was to study the impact of email tracking on privacy.

For my bachelor thesis at the University of Applied Sciences St. Pölten (Austria), I am doing similar research in the German-speaking area, partly building up on Mr. Englehardt's project. 

This project contains various changes in the folder "mailserver". An overview of the changes can be found in the README.md file in the "mailserver" folder.

The project started in early February 2021 and was completed in September 2021. 

## Contact details:
* Student mail: is181036@fhstp.ac.at
* Privat mail: philipp.eichinger98@gmail.com
* GitHub: @peichinger
* LinkedIn: Philipp Eichinger

<br>

*Original README.md:*

# I never signed up for this! Privacy implications of email tracking

This is a public code and data release for the research paper "I never signed
up for this! Privacy implications of email tracking.", which will appear at
PETS 2018. Portions of the code for this project borrow heavily from Jeffrey's
undergraduate senior thesis, available [here](https://github.com/itdelatrisu/thesis).

**Authors:** Steven Englehardt ([@englehardt](https://github.com/englehardt)),
Jeffrey Han ([@itdelatrisu](https://github.com/itdelatrisu)),
and Arvind Narayanan ([@randomwalker](https://github.com/randomwalker))

**Paper:** available [here](https://senglehardt.com/papers/pets18_email_tracking.pdf).

## Components
Core components:
* `crawler_emails/` - A web crawler, built on [OpenWPM](https://github.com/citp/OpenWPM),
    to simulate email views and link clicks.
* `crawler_mailinglists/` - A web crawler, built on [OpenWPM](https://github.com/citp/OpenWPM),
    to find and submit mailing list sign-ups.
* `email-tracking-tester/` - A tool to test the privacy properties of a mail
    client.
* `mailserver/` - The mail server used to collect our corpus of emails.
* `analysis/` - *Coming soon*

## Code Usage

Additional documentation is available in the `README` of each component
subdirectory.

### System Requirements
* The framework is fully tested only on Ubuntu 16.04, and requires Java and
  Python runtime environments.
* The processes (described below) can be run on separate machines. The mail
  server is OS-independent, but the web crawlers only run on Linux.
* Depending on the number of registered sites, the mail server might store
  anywhere from a few hundred megabytes to tens of gigabytes of data on disk
  per month.

### Processes
The system consists of three long-running processes:
* The mail server, which receives, stores, and analyzes incoming mail.
  ```
  $ cd mailsever
  $ mvn clean package
  $ java -jar target/mailserver.jar
  ```
* The mailing list crawler, which crawls a list of input sites and searches for
    mailing lists.
  ```
  $ cd crawler_mailinglists
  $ python crawl_mailinglist_signup.py
  ```
* The email crawler, which renders emails in a simulated webmail environment
    and visits links from those emails.
  ```
  $ cd crawler_emails
  $ python crawl_*.py
  ```

### SMTP Configuration
Running the mail server requires a domain name with MX records pointing to the
server. Additionally, if running the mailing list crawler from machines
other than the mail server's machine, host records (A, CNAME) must also be set.

## Data

The following data used in the analysis is available for download:

### Mailbox
Includes email meta data (subjects, sender, etc) and email body content.

Download link: [mailbox.tar.bz2](https://webtransparency.cs.princeton.edu/email_tracking/mailbox.tar.bz2)

Contents:
* `email_inbox.sqlite`
  * `users` table -- Email address registration records. Maps email address to
      registration site and time.
  * `inbox` table -- Subject, sender, delivery time, and other metadata for
      each email
* `mail/` -- Directory of raw `.eml` files saved by the mail server. Use the
    `inbox` table of the `email_inbox.sqlite` database to navigate.
* `html/` -- HTML bodies parsed from the corresponding raw email bodies. These
    are the HTML emails loaded by the crawlers.
* `html_after_filtering/` -- HTML bodies after filtering tracking tags using
    EasyList and EasyPrivacy. See Section 7 of [the paper](https://senglehardt.com/papers/pets2018_email_tracking.pdf).

### Email view crawl
Crawl data generated by opening the HTML email bodies given in the `html/`
directory of the mailbox using a simulated webmail client. This is the primary
dataset used for the results in Section 4.

Download link: [2017-05-17_email_tracking_view_crawl.sqlite.bz2](https://webtransparency.cs.princeton.edu/email_tracking/2017-05-17_email_tracking_view_crawl.sqlite.bz2)

### Filtered email view crawl
Crawl data generated by opening the HTML email bodies given in the
`filtered_html/` directory of the mailbox using a simulated webmail client.
This is the primary dataset used for the results in the "Server-side email
content filtering" subsection of Section 7.

Download link: [2017-05-28_email_tracking_filtered_view_crawl.sqlite.bz2](#coming-soon)

### Email click crawl
Crawl data generated by visiting a sample of links extracted from the HTML
email bodies of each email in the `html/` directory of the mailbox. This is the
primary dataset used for the results in Section 5.

Download link: [2017-05-17_email_tracking_click_crawl.sqlite.bz2](https://webtransparency.cs.princeton.edu/email_tracking/2017-05-17_email_tracking_click_crawl.sqlite.bz2)

### Mailing list sign-up success rate crawl
Crawl data generated by running our mailing list sign-up procedure on the top
sites, instrumenting the resulting pages to compute the overall level of
successful sign-ups. This is the primary dataset used for the results in the
"Form submission measurement" subsection of Section 3.

Download link: [2017-08-13_signup_success_measurement.sqlite.bz2](#coming-soon)

## Funding

This project was funded by NSF  Grant  CNS  1526353, a research grant
from Mozilla, and Amazon AWS Cloud Credits for Research.
