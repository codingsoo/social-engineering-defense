import json
import nltk.data
import re
import unidecode

tokenizer = nltk.data.load('tokenizers/punkt/english.pickle')

sentence_tokenized_data = {}
count = 0
p = re.compile('[a-z]\.[A-Z]')

with open("punctuated_scam.json","r") as f:
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

            m = p.findall(content)

            for find_word in m:
                print(find_word)
                position = content.find(find_word)
                abbreviation = content[position - 2:position + 2]
                if ("Mr." not in abbreviation) and ("mr." not in abbreviation) and ("Mrs." not in abbreviation) and ("mrs." not in abbreviation) and ("Co." not in abbreviation) and ("co." not in abbreviation) and ("Ms." not in abbreviation) and ("www." not in abbreviation) and ("Www." not in abbreviation) and ("Dr." not in abbreviation) and ("dr." not in abbreviation):
                    print(content)
                    content = content[:position + 2] + ' ' + content[position + 2:]
                    print(content)

            m = p.findall(content)

            scam_content = tokenizer.tokenize(content)
            sentence_tokenized_data[unidecode.unidecode(title)] = scam_content
            count = count + 1
            print(count)

with open("sentence_tokenized_scam2.json","w") as scam_file:
    json.dump(sentence_tokenized_data,scam_file)

print("number of scam data : ", count)
