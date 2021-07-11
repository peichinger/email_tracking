# Info: File contains changes by Philipp Eichinger (@peichinger)

from automation import TaskManager, CommandSequence
from automation.Errors import CommandExecutionError
import crawl_utils as cu
import os

NUM_BROWSERS = 15
NUM_BATCH = 5000
MAIL_DIR = os.path.expanduser('~/EmailTracking/data_html/') #('~/data/html/')  # Directory of HTML Files # changed by PE
EMAIL_DOMAIN = 'eichinger-edv.at' #'lorveskel.me'  # changed by PE

manager_params, browser_params = TaskManager.load_default_params(NUM_BROWSERS)

# Set up server
server = cu.HTTPServer(MAIL_DIR)
server.start()

# Load site list
sites = cu.grab_mail_urls(MAIL_DIR, 8000, EMAIL_DOMAIN) # PE: port added
TOTAL_NUM_SITES = len(sites)

# Configure browser
for i in range(NUM_BROWSERS):
    browser_params[i]['http_instrument'] = True
    browser_params[i]['cookie_instrument'] = True
    browser_params[i]['spoof_mailclient'] = True
    browser_params[i]['headless'] = True

prefix = '2021-07-11_email_tracking_tag_crawl'
manager_params['database_name'] = prefix + '.sqlite'
manager_params['data_directory'] = '~/EmailTracking/output/' # changed by PE
manager_params['log_directory'] = '~/EmailTracking/output/' # changed by PE

# Manage control files
if not os.path.isdir(os.path.expanduser('~/EmailTracking/.openwpm/')): # changed by PE
    os.mkdir(os.path.expanduser('~/EmailTracking/.openwpm/')) # changed by PE
if os.path.isfile(os.path.expanduser('~/EmailTracking/.openwpm/reboot')): # changed by PE
    os.remove(os.path.expanduser('~/EmailTracking/.openwpm/reboot')) # changed by PE
if os.path.isfile(os.path.expanduser('~/EmailTracking/.openwpm/current_site_index')): # changed by PE
    with open(os.path.expanduser('~/EmailTracking/.openwpm/current_site_index'), 'r') as f: # changed by PE
        start_index = int(f.read()) + 1
    end_index = start_index + NUM_BATCH
else:
    start_index = 0
    end_index = NUM_BATCH + 1

# Start crawling
manager = TaskManager.TaskManager(manager_params, browser_params)
current_index = 0
for i in range(start_index, end_index):
    current_index = i
    if current_index >= TOTAL_NUM_SITES:
        break
    try:
        command_sequence = CommandSequence.CommandSequence(sites[i],
                                                           reset=True)
        command_sequence.get(sleep=10, timeout=60)
        command_sequence.get(sleep=10, timeout=60)
        manager.execute_command_sequence(command_sequence)
        with open(os.path.expanduser('~/EmailTracking/.openwpm/current_site_index'),
                  'w') as f:
            f.write(str(i))
    except CommandExecutionError:
        with open(os.path.expanduser('~/EmailTracking/.openwpm/stop'), 'w') as f:
            f.write(str(1))
        break

# Shut down and clean up after batch
manager.close()
server.stop()
cu.clear_tmp_folder()

# Remove index file if we are done
if current_index >= TOTAL_NUM_SITES:
    os.remove(os.path.expanduser('~/EmailTracking/.openwpm/current_site_index'))
    with open(os.path.expanduser('~/EmailTracking/.openwpm/crawl_done'), 'w') as f:
        f.write(str(1))
