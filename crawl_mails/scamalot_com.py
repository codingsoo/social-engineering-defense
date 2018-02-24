import langid
from bs4 import BeautifulSoup
import requests
import json

base_url = 'https://www.scamalot.com/ScamTipReports//'
base_url2 = 'https://www.scamalot.com'
url_data = []
num = 0
num_of_image = 0

for i in range(2218):
    page = i + 1
    print(page)
    req = requests.get(base_url + str(page))
    soup = BeautifulSoup(req.text, 'html.parser')

    for j in range(10):
        data_number = j+1
        scam_content = soup.find(class_=('row' + str(data_number)))
        scam_email = soup.find(class_=('scammerinfo table'))

        if 'Email Address:' in scam_email.text:
            url = soup.find_all('a',attrs={'itemprop':'reportNumber'})
            url_data.append(base_url2+url[j].get('href'))
scam_data = []

for scam_url in url_data:
    try:
        req = requests.get(scam_url)
        soup = BeautifulSoup(req.text, 'html.parser')
        scam_content = soup.find('span',{'itemprop':'articleBody'})
        if 'img' in str(scam_content):
            num_of_image = num_of_image + 1
            print('image found!')
        if langid.classify(scam_content.text)[0] == 'en' and len(scam_content.text) > 10:
            scam_data.append(scam_content.text)
            num = num + 1
            print(num)
    except:
        print(Exception)

with open('scamalot_url_dataset.json','w') as f:
    json.dump(url_data,f)

with open('scamalot_data.json','w') as f:
    json.dump(scam_data,f)
