import langid
from bs4 import BeautifulSoup
import requests
import json

url = 'http://www.scamdex.com/__INCLUDES/__getLatestScamEmails.php'
base_url = 'http://www.scamdex.com'

payload = {'num' : 62700, 'frum' : 0}
response = requests.post(url, data=payload)
soup = BeautifulSoup(response.text, 'html.parser')
scam_url = soup.find_all('a')

url_data = []

for scam_number in range(len(scam_url)):
    if 'email-scam-database' in str(scam_url[scam_number].get('href')):
        url_data.append(scam_url[scam_number].get('href'))

scam_data = []
img_data = []
num_of_image = 0
num = 0

for i, scam_path in enumerate(url_data):
    try:
        req = requests.get(base_url+scam_path)
        soup = BeautifulSoup(req.text, 'html.parser')
        scam_content = soup.find(id='HEADBODYSEP')
        scam_image = scam_content.find('img')
        if scam_image:
            num_of_image = num_of_image + 1
            img_data.append(scam_image.get('alt'))
        if langid.classify(scam_content.text)[0] == 'en' and len(scam_content.text) > 10:
            scam_data.append(scam_content.text)
            print(i, "/", 62700)

            num = num + 1
    except:
        print(Exception)

with open('scamdex_url_dataset.json','w') as f:
    json.dump(url_data,f)

with open('scamdex_data.json','w') as f:
    json.dump(scam_data,f)

with open('scamdex_image_name.json','w') as f:
    json.dump(img_data,f)
