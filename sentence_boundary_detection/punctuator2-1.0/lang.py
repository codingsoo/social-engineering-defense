import json
import os

with open('scam_email_body.json','r') as f:
    data = json.load(f)

    count = 0

    for scam in data:
        if count == 121759:
            print(scam)