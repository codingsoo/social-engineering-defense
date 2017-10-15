import json

scam_body = []

with open('scam_email_with_HTML_tag.json','r') as f:
    data = json.load(f)
    count = 0
    for content in data:
        if '\n' in content:
            email_string = ""
            raw_data = content.split('\n')
            for raw in raw_data:
                if len(raw) < 150:
                    if "From:" in raw:
                        pass
                    elif "from:" in raw:
                        pass
                    elif "Path:" in raw:
                        pass
                    elif "path:" in raw:
                        pass
                    elif "Subject:" in raw:
                        pass
                    elif "Date:" in raw:
                        pass
                    elif "To:" in raw:
                        pass
                    elif "to:" in raw:
                        pass
                    elif "Received:" in raw:
                        pass
                    elif "ID:" in raw:
                        pass
                    elif "Type:" in raw:
                        pass
                    elif "Version:" in raw:
                        pass
                    elif "Encoding:" in raw:
                        pass
                    else:
                        email_string = email_string + raw
                else:
                    email_string = email_string + raw
            scam_body.append(email_string)
        else:
            scam_body.append(content)

for email_body_test in scam_body:
    if(':' in email_body_test):
        print(email_body_test[email_body_test.find(':')-20:email_body_test.find(':')])

print(len(scam_body))

with open('scam_email_body.json','w') as f:
    json.dump(scam_body,f)
