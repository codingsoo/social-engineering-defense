# -*- coding: utf-8 -*-

import json
import codecs
import email.mime.text as Mm
import email.parser
import re

global jdata

with open('scam_email_body_with_HTML.json','rb') as json_file:
    jdata = json.load(json_file)

#jdata_key = jdata.keys()
#jDic = {}
jList = []
parser = email.parser.Message
counter = 0
#for key in jdata_key:
for emailString in jdata:
    #emailString = jdata[key].lstrip().encode('ASCII','ignore')
    emailString = emailString.lstrip().encode('ASCII', 'ignore')

    if(emailString.find("From:") != -1):
        emailString_r = emailString[emailString.find("From:"):]
    elif(emailString.find("from:") != -1):
        emailString_r = "From:" + emailString[emailString.find("from:")+ 6:]
        counter += 1
    else:
        emailString_r = "From: \r\n" + emailString

    msg = email.message_from_string(emailString_r)

    emailContent = ""
    if msg.is_multipart():
        for part in msg.walk():
            temp = str(part.get_payload())
            emailContent += temp
    else:
        emailContent = str(msg.get_payload())

    if(emailContent):  #jDic[key] = emailContent
        jList.append(emailContent)

    print "\n-------------------------------------------------------\n"


    icursor = 0

print str(counter) + '/' + str(len(jdata))
#print len(jDic)
print len(jList)
with open('scams_rid.json','wb') as json_file:
    #json.dump(jDic,json_file)
    json.dump(jList,json_file)
