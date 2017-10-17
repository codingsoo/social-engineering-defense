import json
import nltk.data
import re
import unidecode

tokenizer = nltk.data.load('tokenizers/punkt/english.pickle')

sentence_tokenized_data = []
count = 0
p = re.compile('.\.[a-zA-Z]')
q = re.compile('.\?[a-zA-Z]')

with open("punctuated_scam_pq.json","r") as f:
    data = json.load(f)
    for content in data:
        content = unidecode.unidecode(content)
        content = content.replace(',.','.')

        m = p.findall(content)

        for find_word in m:
            print(find_word)
            position = content.find(find_word)
            period_area = content[position-20:position+28]
            if ('Mr.' not in period_area) and ('Ms.' not in period_area) and ('Mrs.' not in period_area) and ('www.' not in period_area) and ('@' not in period_area) and ('Dr.' not in period_area) and ('mr.' not in period_area) and ('mrs.' not in period_area) and ('dr.' not in period_area) and ('Www.' not in period_area) and ('http' not in period_area) and ('Co.' not in period_area) and ('co.' not in period_area) :
                content = content[:position+2] + ' ' + content[position+2].upper() + content[position+3:]
                print(content[position:position + 4])
        m = q.findall(content)
        for find_word in m:
            print(find_word)
            position = content.find(find_word)
            content = content[:position + 2] + ' ' + content[position + 2].upper() + content[position + 3:]
            print(content[position:position + 4])

        scam_content = tokenizer.tokenize(content)
        sentence_tokenized_data.append(scam_content)

with open("sentence_tokenized_scam.json","w") as scam_file:
    json.dump(sentence_tokenized_data,scam_file)

print("number of scam data : ", count)
