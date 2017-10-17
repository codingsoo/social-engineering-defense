import json

scam_email = []

with open('punctuated_scam_p.json','r') as f:
    data = json.load(f)

    for email in data:
        email = str(email)
        while(True):
            loc = email.find(' ?QUESTIONMARK')

            if (loc == -1) :
                break;

            if loc <= 13:
                punc_s = 0
            else:
                punc_s = loc - 13

            if loc + 21 >= len(email):
                punc_e = len(email)
            else:
                punc_e = loc + 21

            punc_area = email[punc_s:punc_e]

            if punc_area.count('.') == 1:
                email = email.replace(' ?QUESTIONMARK','?', 1)
            else:
                email = email.replace(' ?QUESTIONMARK', '', 1)

        scam_email.append(email)


with open('punctuated_scam_pq.json','w') as f:
    json.dump(scam_email,f)