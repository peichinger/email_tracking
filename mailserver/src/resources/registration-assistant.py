# Program that supports newsletter registration (by Philipp Eichinger, @peichinger)
import requests

print("\n-- Newsletter Registration Assistant --\n\nHINT: Avoid typos at all costs (they affect the automatic analysis)!\n")
cat = None
data = {
    "site": "",
    "url": "",
    "category": ""
}

# Read Input:
site = input("Enter Registration Site Title (or q to quit): ")
while site != "q":
    url = input("Enter Registration Site URL: ")
    category = input("Enter Registration Site Category (or s for the last selected category): ")
    
    if category == "s":
        category = cat
    if category != "s":
        cat = category

    # Check Input:
    if site == None or site == "" or url == None or url == "" or category == None or category == "":
        print("\nERROR: Incorrect or no input!\n")
    else:
        # Generate mail address:
        print("INFO: Try to generate mail address for site: \"" + str(site) + "\", URL: \"" + str(url) + "\", category: \"" + str(category) + "\"")
        data.update({"site": site, "url": url, "category": category})
        resp = requests.post("http://10.0.0.11:8080/register", data=data)

        if resp.status_code == 200:
            email = resp.text.split(";")
            print("\n-> New mail address 1: " + str(email[0]))
            print("-> New mail address 1: " + str(email[1]) + "\n")       
        else:
            print("\nERROR: An error occurred while generating the email address!\n")

    print("\n-- Next Newsletter --\n")
    site = input("Enter Registration Site Title (or q to quit): ")

print("\nProgramm finished!")