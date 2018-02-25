# -*- coding: utf-8 -*-

import json
import codecs
import re
import os
import sys
from nltk.parse import stanford

# be sure there is module(stanford-parser-full-2017-06-09 | stanford-parser-3.8.0-javadoc.jar | stanford-parser-3.8.0-models.jar)
# on the right path (also, existance of 'englishPCFG.ser.gz')and nltk download.
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

# focus on verbal question
# take word from each line and store
def get_question_list_from_word(word_list):

    question_list = []

    for word in word_list:

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

        for phrase in phrase_list:
            question = ""

            for elm in phrase:
                question += tree_to_str(elm)
            # add 'your' to use it for input of QA system
            if "your" not in question:
                question = "your " + question

            question_list.append(question)

    return question_list


def find_form_more_than_two_lines(input_file_name, output_file_name):

    jdata = []
    with open(input_file_name) as json_file:
        jdata = json.load(json_file)

    # deal with some of noisy data
    contextList = ["dear","from","attention","attn","atten","subject","agent","smtp","message-id","click to expand","cc"]

    counter = 0
    qlist = []
    exception_list = []
    for emailString in jdata:
        counter += 1

        if counter % 10 == 0: print counter

        strNum = 0
        lineNumList = []
        emailStringList = emailString.split("\n")
        checkFormFlag = False
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

        if checkFormFlag:
            exception_list.append(counter - 1)

        q_str_list = []
        for string in formLineStr:
            tmp_list = string_pat.findall(string)
            question_list = get_question_list_from_word(tmp_list)

            q_str_list += question_list
        qlist.append(q_str_list)


    OutputFile = codecs.open(output_file_name, "w", encoding='utf-8')

    print counter
    json.dump(qlist, OutputFile)


    with open("result/form_questions_scam_index_0_1000.json","w") as exception_file:
        json.dump(exception_list,exception_file)


    OutputFile.close()

if __name__ == "__main__":

    if (len(sys.argv) > 1):
        input_file_name = sys.argv[1]
    if (len(sys.argv) > 2):
        output_file_name = sys.argv[2]
    else:
        print "[Usage] : [input_file_name][output_file_name]"

    res = find_form_more_than_two_lines(input_file_name, output_file_name)
    print res
