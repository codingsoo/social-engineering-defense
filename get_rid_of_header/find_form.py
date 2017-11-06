# -*- coding: utf-8 -*-

import json
import codecs
import re

global jdata

with open('scam_email_body_with_HTML.json') as json_file:
    jdata = json.load(json_file)

jdata_key = jdata.keys()
OutputFile = codecs.open("form_class.txt","w",encoding='utf-8')

contextList = ["dear","from","attention","attn","atten","subject","agent","smtp","message-id","click to expand"]

counter = 0
checkCnt = 0
numbering = re.compile('(\[*|\(*|\{*)\,*\.*\ *([0-9]+)\,*\.*\ *(\)*|\}*|\]*)\:*\,*\.*\ *[a-zA-Z\(\ \)]+')   # cover "number.~~~~~ / number)~~~~ / number,~~~~ / number}~~~~ / (number)~~~~ / {number}~~~~ ..." form
alphabeting = re.compile('(\[*|\(*|\{*)[a-zA-Z](\)+|\}+|\]+|\.|\,)')                                                # cover "[alpha]. ~~~ / alpha. ~~~ / alpha. ~~~ / alpha), ~~~ / (alpha). ~~~ / {alpha}. ~~~ ..." form
pointing = re.compile('\*+\ *[a-zA-Z]+\ *[a-zA-Z\ ]*')                                                              # cover "*~~~~" form
dotting = re.compile('\.+\ *[a-zA-Z]+\ *[a-zA-Z\ ]*')                                                               # cover ".~~~~" form
nothing = re.compile('([a-zA-Z]+\ *[a-zA-Z\ ]*\ *)\.*\:*\ *\,*(\_\_+\_*|\.\.+\.*|\=\=+\=+|\-\-+\-+)')               # cover "~~~~__________ / ~~~~.......... / ~~~~============ / ~~~:=========== ... " form
colon = re.compile('([a-zA-Z]+\ *\/*\(*[a-zA-Z\ ]*\ *\)*)\:+(\_\_+\_*|\.\.+\.*|\=\=+\=+|\-\-+\-+)*')                # cover "~~~:" form
character = re.compile('([a-zA-Z]|[0-9])+')
spliter = re.compile('(\:+\_*\.*\=*\-*|\_\_\_*|\.\.\.*|\=\=\=*|\-\-\-*)')

for key in jdata_key:
    emailString = jdata[key]
    # if "application" in emailString:
    checkFormFlag = False

    if counter >= 200 : break #############################

    for string in emailString.split("\n"):
        contextFlag = False
        if len(string) > 100: continue
        #
        # if numbering.match(string.lstrip()):
        #     OutputFile.write(str(icursor) + ". **1***" + string + "\n")
        # elif pointing.match(string.lstrip()):
        #     OutputFile.write(str(icursor) + ". **2***" + string + "\n")
        # elif dotting.match(string.lstrip()):
        #     OutputFile.write(str(icursor) + ". **3***" + string + "\n")
        # elif nothing.match(string.lstrip()):
        #     OutputFile.write(str(icursor) + ". **4***" + string + "\n")
        # elif alphabeting.match(string.lstrip()) or colon.match(string.lstrip()):
        #     OutputFile.write(str(icursor) + ". **5***" + string + "\n")

        for elm in contextList:
            if elm in string.lower():
                contextFlag = True

        if contextFlag is False and numbering.match(string.lstrip()) or pointing.match(string.lstrip()) or dotting.match(string.lstrip()) or nothing.match(string.lstrip()) or alphabeting.match(string.lstrip()) or colon.match(string.lstrip()):
            if len(spliter.split(string)) > 2 and character.search(spliter.split(string)[2]):    continue
            else:
                checkFormFlag = True
                OutputFile.write(string + "\n")

    if checkFormFlag:   checkCnt += 1
    OutputFile.write("\n---------------------------------------------------------------------------\n")
    print "%d / %d" % (checkCnt, counter)
    print("\n---------------------------------------------------------------------------\n")
    print emailString
    counter += 1
    print "\n=====================================================================================\n"

print "%d / %d" % (checkCnt, counter)
print "%d / %d" % (counter, len(jdata_key))

OutputFile.close()

