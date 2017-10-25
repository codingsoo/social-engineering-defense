import json

with open('punctuated_scam_pq.json','r') as f:
    data = json.load(f)
    count = 0
    for scam in data:
        count = count + 1

print(count)