import random
import json

scam_email_data = []

with open('sentence_tokenized_scam.json','r') as f:
    scam_email_data = json.load(f)
    random.shuffle(scam_email_data)

print(len(scam_email_data))

with open('shuffled_scam.json','w') as f:
    json.dump(scam_email_data,f)

training_scam = scam_email_data[:150000]
test_scam = scam_email_data[150000:]

with open('training_scam.json','w') as f:
    json.dump(training_scam,f)

with open('test_scam.json','w') as f:
    json.dump(test_scam,f)
