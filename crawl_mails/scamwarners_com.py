import requests
from bs4 import BeautifulSoup
import json
import os

# json file download directory
BASE_DIR = os.path.dirname(os.path.abspath(__file__))

def match_class(target):
    def do_match(tag):
        classes = tag.get('class', [])
        return all(c in classes for c in target)
    return do_match

# extact url's titles
url = 'https://www.scamwarners.com/forum'

req = requests.get(url)
html = req.text
soup = BeautifulSoup(html, 'html.parser')
titles = soup.select('td > a')

titles = titles[21:]

# show titles
print("-*-*-*-*-*-Scam List-*-*-*-*-*-")
for count, title in enumerate(titles):
    if count % 3 == 0:
        print(int(count/3)," : ",title.text)
print("-*-*-*-*-*-Scam List End-*-*-*-*-*-")
print()

# user can select the titles(with range or one by one)
select_scam_input = input("select mode = 1 : some scams 2 : cluster mode")

if(select_scam_input == '1'):
    select_scam_input = input("select scam number that you want to download(seperator : ,) : ")
    select_scam_input = select_scam_input.split(",")
else:
    select_scam_input = input("select range of number that you want to download(seperator : -) : ")
    number = select_scam_input.split("-")
    number1 = int(number[0])
    number2 = int(number[1])
    select_scam_input = range(number1,number2+1)

# append all of the pages' selected path url
path = []

for scam_page in select_scam_input:
    page_url = url+titles[int(scam_page)*3].get('href')[1:]
    path.append(page_url)

    req = requests.get(page_url)
    html = req.text
    soup = BeautifulSoup(html, 'html.parser')
    pages = soup.select('li > a')

    num_of_pages = int(pages[-6].text)
    print(num_of_pages)

    page_path = ""
    i=1
    while titles[int(scam_page) * 3].get('href')[i] != 's':
        page_path = page_path + titles[int(scam_page) * 3].get('href')[i]
        i = i+1

    if (num_of_pages < 2):
        pass
    else:
        for page in range(1, num_of_pages):
            page_url_path = url + page_path + "start="+str(page*50)
            path.append(page_url_path)

# append all of the url's data(exact phishing mail)
data = {}

for data_path in path[:-2]:

    req = requests.get(data_path)
    html = req.text
    soup = BeautifulSoup(html, 'html.parser')
    pages = soup.select('td > a')

    for page in pages:
        if "p=0" in page.get('href')[1:]:
            continue
        if "viewtopic" in page.get('href') and "WARNING" not in page.text:
            data[page.text] = url + page.get('href')[1:]

num_of_phishing_mail = 0
num_of_image = 0

# append all of the phishing mail data(exact phishing mail data)
phishing_mail_data = {}
for phishing_name, phishing_url in data.items():
    try:
        req = requests.get(phishing_url)
        html = req.text
        soup = BeautifulSoup(html, 'html.parser')
        article = soup.find_all(match_class(["content"]))
        if 'From:' in article[0].text:
            num_of_phishing_mail = num_of_phishing_mail + 1
            print(num_of_phishing_mail)
            phishing_mail_data[phishing_name] = article[0].text

            if (("img" in str(article[0])) and ("icon" not in str(article[0]))):
                num_of_image = num_of_image + 1
                print("image found")

    except:
        print(Exception)

print("Total number of mail data :",num_of_phishing_mail)
print("Total number of image data :",num_of_image)

# store title and phishing mail data
with open(os.path.join(BASE_DIR, 'scamwarners.json'), 'w+') as json_file:
    json.dump(phishing_mail_data, json_file)
