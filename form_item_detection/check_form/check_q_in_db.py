# -*- coding: utf-8 -*-

import requests
import json
import re
import time
import os
import sys


def check_from_web(questions, output_file_name):
    url = 'http://0.0.0.0:8083/parse?sent='
    quote = re.compile("\"[a-z0-9\-\.]*\"")

    checked_cnt = 0
    checked_list = []
    index = 0
    for question_from_email in questions:
        if index % 1000 == 0:
            print index


        email_check_flag = False
        for question in question_from_email:
            question_str = question.replace(" ","+")
            question_str = "what+is+"+question_str

            response = requests.get(url+question_str)
            result = response.json()

            for idx in range(0,15):
                try:
                    query_result = result['result'][idx]['answers']
                    if(len(query_result) != 0):
                        email_check_flag = True
                        break
                except:
                    break

        if email_check_flag:
            checked_cnt += 1
        checked_list.append(email_check_flag)
        index += 1

    with open("output_file_name","w") as ouput_f:
        json.dump(checked_list,ouput_f)
    
    print "checking count is %d" %checked_cnt

if __name__ == "__main__":

    if (len(sys.argv) > 1):
        input_file_name = sys.argv[1]
    if (len(sys.argv) > 2):
        output_file_name = sys.argv[2]
    else:
        print "[Usage] : [input_file_name][output_file_name]"
    
    question_list = []

    with open("input_file_name") as question_file:
        question_list = json.load(question_file)

    check_from_web(question_list, output_file_name)
