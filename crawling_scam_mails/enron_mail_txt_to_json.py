from os import listdir
from os.path import isfile, join
import codecs
import re
import json

Form = ["From:", "Sent by:", "from","Path:", "path","Subject:", "Date:", "To:","to:","Received:","ID:","Type:","Encoding","Version:"]

def check_form(line):
    for f in Form:
        if f in line: return True
    return False


def read_mail(mail_path):
    f = codecs.open(mail_path, "r", encoding="utf-8", errors='ignore')
    temp = f.readline()

    #remove header
    while(temp != "\n" and temp != ""):
        temp = f.readline().strip()
        pass
    result = ""
    r = re.compile(r"([a-zA-Z0-9\/\_ ]+@[a-zA-Z0-9\/\_ ]+,){2,}")
    for line in f.readlines():
        #remove email list
        if(r.search(line)): pass

        #remove form
        elif(check_form(line) == True or line == "\n"): pass

        #remove special line
        elif("----" in line): pass

        else: result += line
    return result

def write_Json(file_name, file_path):
    list = []
    wF = open(file_name, "w")
    for name in listdir(file_path):
        print("\n" + name , end=" ")
        mypath_name = join(file_path,name)
        if (isfile(mypath_name)): continue

        for filedir in listdir(mypath_name):
            print(filedir, end =" ")
            mypath_name_dir = join(mypath_name, filedir)
            if (isfile(mypath_name_dir)): continue
            for file in listdir(mypath_name_dir):
                mail_path = join(mypath_name_dir, file)
                if not isfile(mail_path): continue
                list.append(read_mail(mail_path))
    json.dump(list, wF)
    wF.close()

def read_Json(file_name, file_path):
    rF = open(file_name, "r")
    data = json.load(rF)

    #test
    for d in data:
        print(d)

    return data

my_path = "C://Users//kimhyeji//Downloads//stanford-parser-full-2017-06-09//stanford-parser-full-2017-06-09//src//maildir//maildir"
my_file_name = "data.json"
#write_Json(my_file_name,my_path)
read_Json(my_file_name,my_path)
