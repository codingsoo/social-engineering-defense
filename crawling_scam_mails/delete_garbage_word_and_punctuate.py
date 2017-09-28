import json
import nltk.data
import re
import subprocess
import os
import unidecode

tokenizer = nltk.data.load('tokenizers/punkt/english.pickle')
FNULL = open(os.devnull, 'w')

punctuated_data = {}
count = 0

with open("refined_scam.json","r") as f:
    data = json.load(f)
    for title, content in data.items():
        if len(content) > 30:
            content = str(content).replace("b'", "")
            content = re.sub('\\\\x..', '', content)
            content = content.replace("\"", "")
            content = content.replace("\\", "")
            content = content.replace("\\\"", "")
            content = content.replace("\n", "")
            content = content.replace("\r", "")
            content = content.replace("\t", "")
            content = content.replace("&", "")
            content = content.replace(";", "")
            content = unidecode.unidecode(content)
            process = subprocess.Popen('curl -d \"text=%s\" http://bark.phon.ioc.ee/punctuator' % (content), shell=True, universal_newlines=False, stdout=subprocess.PIPE, stderr=FNULL)
            output = process.communicate()
            punctuated_data[unidecode.unidecode(title)] = str(output[0]).replace("b\"", "")
            count = count + 1
            print(count)

with open("garbage_word_deleted_and_punctuated_scam.json","w") as scam_file:
    json.dump(punctuated_data,scam_file)

print("number of scam data : ", count)
