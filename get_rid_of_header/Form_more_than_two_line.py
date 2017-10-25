# -*- coding: utf-8 -*-

import json
import codecs
import re

global jdata

with open('merged_scams_rid.json') as json_file:
    jdata = json.load(json_file)

jdata_key = jdata.keys()
OutputFile = codecs.open("form_class_two_line.txt","w",encoding='utf-8')

contextList = ["dear","from","attention","attn","atten","subject","agent","smtp","message-id","click to expand"]

counter = 0
checkCnt = 0
numbering = re.compile('(\[*|\(*|\{*)\,*\.*\ *([0-9]+)\,*\.*\ *(\)*|\}*|\]*)\:*\,*\.*\.*\ *[a-zA-Z\(\ \)]+')   # cover "number.~~~~~ / number)~~~~ / number,~~~~ / number}~~~~ / (number)~~~~ / {number}~~~~ ..." form
alphabeting = re.compile('(\[*|\(*|\{*)[a-zA-Z](\)+|\}+|\]+|\.|\,)')                                                # cover "[alpha]. ~~~ / alpha. ~~~ / alpha. ~~~ / alpha), ~~~ / (alpha). ~~~ / {alpha}. ~~~ ..." form
pointing = re.compile('\*+\ *[a-zA-Z]+\ *[a-zA-Z\ ]*')                                                              # cover "*~~~~" form
dotting = re.compile('\.+\ *[a-zA-Z]+\ *[a-zA-Z\ ]*')                                                               # cover ".~~~~" form
nothing = re.compile('([a-zA-Z]+\ *[a-zA-Z\ ]*\ *)\.*\:*\ *\,*(\_\_+\_*|\.\.+\.*|\=\=+\=+|\-\-+\-+)')               # cover "~~~~__________ / ~~~~.......... / ~~~~============ / ~~~:=========== ... " form
colon = re.compile('([a-zA-Z]+\ *\/*\(*[a-zA-Z\ ]*\ *\)*)\:+(\_\_+\_*|\.\.+\.*|\=\=+\=+|\-\-+\-+)*')                # cover "~~~:" form
character = re.compile('([a-zA-Z]|[0-9])+')
spliter = re.compile('(\:+\_*\.*\=*\-*|\_\_\_*|\.\.\.*|\=\=\=*|\-\-\-*||)')

for key in jdata_key:
    emailString = jdata[key]
    checkFormFlag = False
    strNum = 0
    lineNumList = []

    # if counter >= 200: break#################

    emailStringList = emailString.split("\n")
    for string in emailStringList:
        contextFlag = False
        if len(string) > 100: pass
        else:
            for elm in contextList:
                if elm in string.lower():
                    contextFlag = True

            if contextFlag is False and numbering.match(string.lstrip()) or pointing.match(string.lstrip()) or dotting.match(string.lstrip()) or nothing.match(string.lstrip()) or alphabeting.match(string.lstrip()) or colon.match(string.lstrip()):
                if len(spliter.split(string)) > 2 and character.search(spliter.split(string)[2]):    pass
                else:
                    lineNumList.append(strNum)

        strNum += 1

    formLineNum = []
    for i in lineNumList:
        if emailStringList[i] != "\n" or emailStringList[i] != "\r\n" or emailStringList[i] == "":
            formLineNum.append(i)

    formLineStr = []
    for i in range(len(formLineNum) - 1):
        if formLineNum[i] + 1 == formLineNum[i+1]:
            formLineStr.append(emailStringList[formLineNum[i]])
            if i == len(formLineNum) - 2 and formLineNum[i] + 1 == formLineNum[i+1]:
                formLineStr.append(emailStringList[formLineNum[i+1]])

            checkFormFlag = True

    ############################
    # OutputFile.write(str(formLineNum) + "\n")
    for string in formLineStr:
        OutputFile.write(string + "\n")
    #############################



    if checkFormFlag:   checkCnt += 1
    # OutputFile.write("\n\n\n\n\n\n")
    #
    # sc = 0
    # for string in emailStringList:
    #     OutputFile.write(str(sc) + ". "+ string + "\n")
    #     sc += 1

    OutputFile.write("\n---------------------------------------------------------------------------\n")
    print "%d / %d" % (checkCnt, counter)
    print("\n---------------------------------------------------------------------------\n")
    print emailString
    counter += 1
    print "\n=====================================================================================\n"

print "%d / %d" % (checkCnt, counter)
print "%d / %d" % (counter, len(jdata_key))

OutputFile.close()

