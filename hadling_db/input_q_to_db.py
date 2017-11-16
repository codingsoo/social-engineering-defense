import requests
import json
import re
import sqlite3

def get_query_from_web(cursor, questions):
    url = 'http://0.0.0.0:8083/parse?sent='
    quote = re.compile("\"[a-z0-9\-\.]*\"")

    ##question = 'parse?sent=who+is+obama' ###
    for question_from_email in questions:
        for question in question_from_email:
            question_str = question.replace(" ","+")
            print question_str


            response = requests.get(url+question_str)
            result = response.json()

            for idx in range(0,4):
                try:
                    query_result = result['result'][idx]['query']

                    find_arg = query_result.find('SELECT') + 7
                    confidential = query_result[find_arg:find_arg+4]

                    db_list = quote.findall(query_result)

                    cursor.execute(query_result)

                    rows = cursor.fetchall()
                    print "\n--------------------\n%s\n this is query result : " %(query_result)
                    for row in rows:
                        print(row)
                    print "\n--------------------\n"

                    if confidential == "arg1":
                        input_query = "INSERT INTO tuples (rel, arg1, agr2) VALUES(" + db_list[0] + "," + "confidential.e" + " ," + db_list[1] + ");"
                    elif confidential == "arg2":
                        input_query = "INSERT INTO tuples (rel, arg1, agr2) VALUES(" + db_list[0] + "," + db_list[1] + " ," + "confidential.e" + ");"
                    else:
                        print "error : confidential is " + confidential + "\n"
                    #cursor.execute(input_query)
                    print(input_query)

                except:
                    break


question_list = []

with open("questions_scam/questions.json") as question_file:
    question_list = json.load(question_file)

conn = sqlite3.connect('newtuples.db')
curs = conn.cursor()

get_query_from_web(curs, question_list)