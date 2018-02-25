# -*- coding: utf-8 -*-
# python 2.7

import json
import codecs
import email.mime.text as Mm
import email.parser
import re
import sys


def get_rid_of_header_of_email(input_file_name, output_file_name):
    #load data from input file (whole mail texts which contain html tags)
    jdata = []
    jList = []
    with open(input_file_name,'rb') as json_file:
        jdata = json.load(json_file)

    # parser = email.parser.Message
    counter = 0
    # get rid of header of email text
    # some of email are just in a line skip them for this research
    for emailString in jdata:
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

        icursor = 0

    print str(counter) + '/' + str(len(jdata))
    # dump to output email content text
    with open(output_file_name,'wb') as json_file:
        json.dump(jList,json_file)

    return {'result' : "Done"}

if __name__ == "__main__":
    
    if (len(sys.argv) > 1):
          input_file_name = sys.argv[1]
    if (len(sys.argv) > 2):
          output_file_name = sys.argv[2]
    else:
        print "[Usage] : [input_file_name][output_file_name]"
        
    res = get_rid_of_header_of_email(input_file_name,output_file_name)
    print res
