import requests
from bs4 import BeautifulSoup
import json
import os

BASE_DIR = os.path.dirname(os.path.abspath(__file__))

url = "http://www.scamalot.com/ScamTipReports/"
req = requests.get(url)
html = req.text
soup = BeautifulSoup(html, 'html.parser')
titles = soup.select('select > option')
my_titles = titles[1:25]

# show titles
print("-*-*-*-*-*-Scam List-*-*-*-*-*-")
for count, title in enumerate(my_titles):
    print(count," : ",title.text)
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
    number1 = number[0]
    number2 = number[1]
    select_scam_input = range(number1,number2+1)

# append all of the pages' selected path url
path = []

for scam_page in select_scam_input:
    page_url = url + my_titles[int(scam_page)].get('value')
    path.append(page_url)
    req = requests.get(page_url)
    html = req.text
    soup = BeautifulSoup(html, 'html.parser')
    pages = soup.select('div > a')
    num_of_pages = int(pages[-3].text)

    if(num_of_pages < 2):
        pass
    else:
        for page in range(2,num_of_pages):
            page_url_path = page_url + "/" + str(page)
            path.append(page_url_path)

base_url = "http://www.scamalot.com"

# append all of the url's data(exact phishing mail)
data = {}

for data_path in path:

    req = requests.get(data_path)
    html = req.text
    soup = BeautifulSoup(html, 'html.parser')
    pages = soup.select('td > a')

    for page in pages:

        if (("ScamTipReports" in page.get('href')) and ("more" not in page.text)):
            data[page.text[:-3]] = page.get('href')
num_of_phishing_mail = 0


# append all of the phishing mail data(exact phishing mail data)
phishing_mail_data = {}
num_of_image = 0
for phishing_name, phishing_url in data.items():

    num_of_phishing_mail = num_of_phishing_mail + 1

    req = requests.get(base_url+phishing_url)
    html = req.text
    soup = BeautifulSoup(html, 'html.parser')
    phishing_data = soup.select('td > span')

    for exect_data in phishing_data:
        if exect_data.get('itemprop'):
            phishing_mail_data[phishing_name] = exect_data.text
            if("img" in str(exect_data)):
                num_of_image = num_of_image + 1

print("Total number of mail data :",num_of_phishing_mail)
print("Total number of image :",num_of_image)

# store title and url
# with open(os.path.join(BASE_DIR, 'scamalot_result.json'), 'w+') as json_file:
#     json.dump(data, json_file)

# store title and phishing mail data
with open(os.path.join(BASE_DIR, 'scamalot_wills_probate_scam.json'), 'w+') as json_file:
    json.dump(phishing_mail_data, json_file)


