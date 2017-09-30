# -*- coding: utf-8 -*-

import json
import codecs
import re

global jdata

with open('merged_scams_rid.json') as json_file:
    jdata = json.load(json_file)

jdata_key = jdata.keys()
OutputFile = codecs.open("form_class.txt","w",encoding='utf-8')

exceptList = ["return-path","return path","received","date","sent","mailed-by","signed-by","orig-id",
              "x-originating-ip","x-sender","x-received","for","x-sid-pra","x-aimc-mailfrom","x-aol-date","x-me-user-auth","x-fii-tracking",
              "x-originatingip","received from","note","mail-reply-to","to","reply-to","<http","http","https", "reply to",
              "om", "delivered-to","message-id", "mime-version", "content-type", "content-transfer-encoding",
              "user-agent" ,"sender","our ref","isp","cc","executive secretary","no","ref number","header"]
contextList = ["dear","from","attention","attn","atten","subject","agent","smtp","message-id","click to expand"]

counter = 0
numbering = re.compile('(\[*|\(*|\{*)\,*\.*\ *([0-9]+)\,*\.*\ *(\)*|\}*|\]*)\,*\.*\ *[a-zA-Z]+\ *[a-zA-Z\ ]*')  # cover "number.~~~~~ / number)~~~~ / number,~~~~ / number}~~~~ / (number)~~~~ / {number}~~~~" form
alphabeting = re.compile('(\[*|\(*|\{*)[a-zA-Z](\)+|\}+|\]+|\.|\,)')
pointing = re.compile('\*+\ *[a-zA-Z]+\ *[a-zA-Z\ ]*')                        # cover "*~~~~" form
dotting = re.compile('\.+\ *[a-zA-Z]+\ *[a-zA-Z\ ]*')                         # cover ".~~~~" form
nothing = re.compile('([a-zA-Z]+\ *[a-zA-Z\ ]*\ *)\.*\:*\ *\,*(\_\_+\_*|\.\.+\.*|\=\=+\=+|\-\-+\-+)')      # cover "~~~~__________ / ~~~~.......... / ~~~~============
colon = re.compile('([a-zA-Z]+\ *[a-zA-Z\ ]*\ *)\:+(\_\_+\_*|\.\.+\.*|\=\=+\=+|\-\-+\-+)*')                 #cover "~~~:"
for key in jdata_key:
    emailString = jdata[key]
    icursor = 0
    # if "application" in emailString:
    if "form" in emailString:
        for string in emailString.split("\n"):
            contextFlag = False
            if len(string) > 100: continue
            icursor += 1

            if numbering.match(string.lstrip()):
                OutputFile.write(str(icursor) + ". **1***" + string + "\n")
            elif pointing.match(string.lstrip()):
                OutputFile.write(str(icursor) + ". **2***" + string + "\n")
            elif dotting.match(string.lstrip()):
                OutputFile.write(str(icursor) + ". **3***" + string + "\n")
            elif nothing.match(string.lstrip()):
                OutputFile.write(str(icursor) + ". **4***" + string + "\n")
            elif alphabeting.match(string.lstrip()) or colon.match(string.lstrip()):
                OutputFile.write(str(icursor) + ". **5***" + string + "\n")



            elif ":" in string and string.split(':')[0].lstrip().lower() not in exceptList\
                or "..." in string and string.split('...')[0].lstrip().lower() not in exceptList \
                    or "__" in string and string.split('__')[0].lstrip().lower() not in exceptList:    #print string.split(':')[0]

                for elm in contextList:
                    if elm in string.lower():
                        contextFlag = True

                if contextFlag is False:
            #        print str(icursor) + ". " + string
                    OutputFile.write(str(icursor) + ". " +string + "\n")
    OutputFile.write("\n---------------------------------------------------------------------------\n")
    print counter
    print("\n---------------------------------------------------------------------------\n")
    print emailString
    counter += 1
    print "\n=====================================================================================\n"


print "%d / %d" % (counter, len(jdata_key))

OutputFile.close()

