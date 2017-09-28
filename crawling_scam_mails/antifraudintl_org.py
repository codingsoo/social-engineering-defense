import requests
from bs4 import BeautifulSoup
import json
import os

# reference_blog : https://beomi.github.io/2017/01/20/HowToMakeWebCrawler/

# It can search the url's title
def request_titles(url):
    req = requests.get(url)
    html = req.text
    soup = BeautifulSoup(html, 'html.parser')
    titles = soup.select('h3 > a')

    return titles

# It can search the url's nav
def request_nav(url):
    req = requests.get(url)
    html = req.text
    soup = BeautifulSoup(html, 'html.parser')
    nav = soup.select('nav > a')

    return nav

# It can search the url's article
def request_article(url):
    req = requests.get(url)
    html = req.text
    soup = BeautifulSoup(html, 'html.parser')
    article = soup.select('article > blockquote')

    return article

def request_image(url):
    req = requests.get(url)
    html = req.text
    soup = BeautifulSoup(html, 'html.parser')
    image = soup.select('a > img')

    return image

# json file download directory
BASE_DIR = os.path.dirname(os.path.abspath(__file__))

# extact url's titles
url = 'http://antifraudintl.org/'

my_titles = request_titles(url)

# show titles
print("-*-*-*-*-*-Scam List-*-*-*-*-*-")
print("warning : 21 has error")
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
    number1 = int(number[0])
    number2 = int(number[1])
    select_scam_input = range(number1,number2+1)

print("input :",select_scam_input)

# append all of the pages' selected path url
path = []
num_of_page = 0

for scam_page in select_scam_input:
    page_url = my_titles[int(scam_page)].get('href')
    path.append(page_url)
    nav = request_nav(url+page_url)

    # for one page urls
    try:
        num_of_page = int(nav[3].text)
    except:
        num_of_page = 0
        pass
    if(num_of_page > 1):
        for page in range(2,num_of_page+1):
            page_url = my_titles[int(scam_page)].get('href') + "page-" + str(page)
            path.append(page_url)
            print("path appended :",page_url)

print("path :",path)

# append all of the url's data(exact phishing mail)
data = {}

for data_path in path:

    print("processing path----------------------->",data_path)
    my_titles = request_titles(url + data_path)

    for title in my_titles:
        # Tag안의 텍스트 => title.txt
        # Tag의 속성을 가져오기(ex: href속성) => title.get('href')

        data[title.text] = title.get('href')
        print("data appended :",title.text)
    # eliminate unnecessary data
    data.pop('Thread Display Options')
    print("processed data------------------------->",data)

num_of_phishing_mail = 0
num_of_images = 0

# append all of the phishing mail data(exact phishing mail data)
phishing_mail_data = {}
for phishing_name, phishing_url in data.items():
    num_of_phishing_mail = num_of_phishing_mail + 1
    article = request_article(url + phishing_url)
    print("crawled mail : ",article[0].text)
    if "attachments" in str(article[0]):
        num_of_images = num_of_images + 1

    phishing_mail_data[phishing_name] = article[0].text

print("Total number of mail data :", num_of_phishing_mail)
print("Total image mail :", num_of_images)

# store title and url
# with open(os.path.join(BASE_DIR, 'antifraudintl_result.json'), 'w+') as json_file:ㅂ
#     json.dump(data, json_file)

# store title and phishing mail data
with open(os.path.join(BASE_DIR, 'antifraudintl_widows.json'), 'w+') as json_file:
    json.dump(phishing_mail_data, json_file)
