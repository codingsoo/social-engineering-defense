# -*- coding: utf-8 -*-

import json
import codecs
import re
import os
from nltk.parse import stanford


os.environ['CLASSPATH'] = '/Users/hessong/Documents/stanford-parser-full-2017-06-09/'
os.environ['STANFORD_PARSER'] = '/Users/hessong/Documents/stanford-parser-full-2017-06-09/stanford-parser-3.8.0-javadoc.jar'
os.environ['STANFORD_MODELS'] = '/Users/hessong/Documents/stanford-parser-full-2017-06-09/stanford-parser-3.8.0-models.jar'

parser = stanford.StanfordParser(model_path="edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz")

numbering = re.compile('(\[*|\(*|\{*)\,*\.*\ *([0-9]+)\,*\.*\ *(\)*|\}*|\]*)\:*\,*\.*\.*\ *[a-zA-Z\(\ \)]+')        # cover "number.~~~~~ / number)~~~~ / number,~~~~ / number}~~~~ / (number)~~~~ / {number}~~~~ ..." form
alphabeting = re.compile('(\[*|\(*|\{*)[a-zA-Z](\)+|\}+|\]+|\.|\,)')                                                # cover "[alpha]. ~~~ / alpha. ~~~ / alpha. ~~~ / alpha), ~~~ / (alpha). ~~~ / {alpha}. ~~~ ..." form
pointing = re.compile('\*+\ *[a-zA-Z]+\ *[a-zA-Z\ ]*')                                                              # cover "*~~~~" form
dotting = re.compile('\.+\ *[a-zA-Z]+\ *[a-zA-Z\ ]*')                                                               # cover ".~~~~" form
nothing = re.compile('([a-zA-Z]+\ *[a-zA-Z\ ]*\ *)\.*\:*\ *\,*(\_\_+\_*|\.\.+\.*|\=\=+\=+|\-\-+\-+)')               # cover "~~~~__________ / ~~~~.......... / ~~~~============ / ~~~:=========== ... " form
colon = re.compile('([a-zA-Z]+\ *\/*\(*[a-zA-Z\ ]*\ *\)*)\:+(\_\_+\_*|\.\.+\.*|\=\=+\=+|\-\-+\-+)*')                # cover "~~~:" form

character = re.compile('([a-zA-Z]|[0-9])+')
spliter = re.compile('(\:+\_*\.*\=*\-*|\_\_\_*|\.\.\.*|\=\=\=*|\-\-\-*||)')
string_pat = re.compile('[a-zA-Z]+\-?[a-zA-Z\ ]*')
white_space = re.compile('\s')

check_s_n = re.compile('\(NP')
check_s_v = re.compile('\(VP')

def get_NP(phrases):
    NP_list =[]
    #print phrases
    if '(' not in str(phrases) and ')' not in str(phrases):
        return
    else:
        if check_s_n.match(str(phrases)):
            for elm in phrases:
                NP_list.append(elm)
        else:
            for phrase in phrases:
                if get_NP(phrase):
                    NP_list.append(get_NP(phrase))

    return NP_list

def get_VP(phrases):
    VP_list =[]
    #print phrases
    if '(' not in str(phrases) and ')' not in str(phrases):
        return
    else:
        if check_s_v.match(str(phrases)):
            for elm in phrases:
                VP_list.append(elm)
        else:
            for phrase in phrases:
                if get_VP(phrase):
                    VP_list.append(get_VP(phrase))

    return VP_list

def tree_to_str(tree):
    result_str = ""

    if '(' not in str(tree) and ')' not in str(tree):
        return str(tree)
    else:
        for elm in tree:
            result_str += tree_to_str(elm) + " "

    return result_str

def get_question_list_from_word(word_list):

    question_list = []

    for word in word_list:

        #print "word is " + word
        ## 이 부분에 가장 작은 단위의 NP를 뽑아낼 수 있도록, 이후 np에 your이 없으면 추가한 후 계산
        word = word.lower().replace("your ","")
        if not word:
            continue
        temp = parser.raw_parse(word)
        depth = 0

        phrases = list(temp)[0]         ## first raw_parse takes one input setecne, chop off the root

        count = 0
        phrase_list = []

        for phrase in phrases:
            count += 1
            if len(get_NP(phrases)) != 0: phrase_list.append(get_NP(phrase))
            if len(get_VP(phrases)) != 0: phrase_list.append(get_VP(phrase))

        # print phrases
        for phrase in phrase_list:
            # print "phrase is"
            # print phrase
            # print "phrase end"

            question = ""
            # print "each is",

            for elm in phrase:
                question += tree_to_str(elm)

            # print question

            if "your" not in question:
                question = "your " + question

            question_list.append(question)

    return question_list


global jdata

with open('scam_email_body_with_HTML.json') as json_file:
    jdata = json.load(json_file)

OutputFile = codecs.open("form_questions.txt","w",encoding='utf-8')

contextList = ["dear","from","attention","attn","atten","subject","agent","smtp","message-id","click to expand"]


# counter = 0
# checkCnt = 0
for emailString in jdata:

    checkFormFlag = False
    strNum = 0
    lineNumList = []
    emailStringList = emailString.split("\n")

    for string in emailStringList:
        contextFlag = False
        if len(string) > 100: pass
        else:
            for elm in contextList:
                if elm in string.lower():
                    contextFlag = True

            if contextFlag is False and numbering.match(string.lstrip()) or pointing.match(string.lstrip()) \
                    or dotting.match(string.lstrip()) or nothing.match(string.lstrip()) \
                    or alphabeting.match(string.lstrip()) or colon.match(string.lstrip()):
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
    for string in formLineStr:
        tmp_list = string_pat.findall(string)
        question_list = get_question_list_from_word(tmp_list)

        for question in question_list:
            OutputFile.write(str(question) + "\n")
    #############################
    # need to modi with output handling to find out scam email form email set
    ############################

    OutputFile.write("\n---------------------------------------------------------------------------\n")

OutputFile.close()

