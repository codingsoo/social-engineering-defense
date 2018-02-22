from nltk.wsd import lesk
from nltk import sent_tokenize
from nltk.corpus import wordnet as wn
import math


#count keywords in mail
def counting_mail(file):
    data = {}
    reader = open(file,'r')
    r = reader.readlines();
    for line in r:
        words = line.split("\n")[0].split(",")
        for word in words:
            word=word.lower().split("/")[0]
            if(word == ""): continue
            if(word in data.keys()):
                data[word] += 1
            else:
                data[word] = 1
    return data

#count keywords in keyword_file
def counting_keyword(file):
    data = open(file , "r").readlines()
    dic = {}
    for i in data:
        i = i.lower().split("/")[0].split("\n")[0]
        if i in dic.keys():
            dic[i] += 1
        else:
            dic[i] = 1
    return dic

#combine counting dictionary
def count_combine(s_dic, e_dic):
    max_count = 0
    count = 0
    comb_dic = {}
    enron_keys = e_dic.keys()
    for k, v in s_dic.items():
        count += 1
        max_count = max(max_count, v)
        if k in enron_keys:
            comb_dic[k] = [v,e_dic[k]]
        else:
            comb_dic[k] = [v,0]
    comb_dic['MAX'] = [max_count,count]
    return comb_dic

#tf : word counting in whole spam mail
#idf : log(whole mail count / mail counting + 1)
#mail counting : counting in scam mail + counting in enron mail * weight
def tfidf(scam_dic, enron_dic, words, mail):
    enron_keys = enron_dic.keys()
    mail_keys = mail.keys()
    result = []
    enron_weight = 1000
    for k, v in words.items():
        #maximum tf normalization in scam mail
        #a_w for smoothing 
        if(k == 'MAX'): continue
        if(k in mail_keys):
            idf = math.log(200000/(mail[k][0]+enron_weight*mail[k][1]+words[k][1]+1))
        else:
            idf = 0.5 * math.log(200000/(words[k][0]+1000*words[k][1]+1))

        tf = v[0]
        result.append([k, tf * idf])
    result.sort(key = lambda x : x[1])
    return result

def write_blacklist(threshold_num, result, scam_dic, enron_dic):
    all_num = len(result)
    start_num = all_num - int(threshold_num * all_num)
    write_str = "tfdif_"+str(threshold_num)+"_"+str(all_num-start_num)+".txt"
    with open(write_str, "w") as w:
        #test version
        if(threshold_num == 0):
            enron_keys = enron_dic.keys()
            for i in result:
                if(i[0] not in enron_keys):
                    w.write(i[0] + " " + str(0.001*(int(i[1]*1000))) +" " + str(scam_dic[i[0]]) + " " + str(0) + "\n")      
                else:
                    w.write(i[0] + " " + str(0.001*(int(i[1]*1000))) +" " + str(scam_dic[i[0]]) + " " + str(enron_dic[i[0]]) + "\n")
        #store version
        else:
            for j in range(start_num,all_num):
                i = result[j]
                w.write(i[0] + "\n")
    return write_str



def add_synonym(word, sent):
    lesk_w = lesk(sent, word)
    if (lesk_w):
        temp = lesk_w.lemma_names()
        for t in temp:
            if(len(t.split("_")) > 1):
                temp.remove(t)
        return temp
    return ""

def store(w,verb, obj):
    for v in verb:
        for o in obj:
            if o and v:
                w.write(v.lower() + ' ' + o.lower() + "\n")
    return len(verb)

def make_wordlist(word, sent):
    word_list = []
    word_list.append(word)
    word_list += add_synonym(word, sent)
    return set(word_list)

def expand_wordlist(input_file):
    output_file = "all_" + file
    r = open(input_file,"r")
    w = open(output_file, "w")
    count = 0
    for sent in r.readlines():
        verb = []
        obj = []
        s = sent.split("\n")[0].split(" ")
        if(len(s) < 2): continue
        count += store(w,make_wordlist(s[0].lower(),sent),make_wordlist(s[1].lower(),sent))
    print(count)
    r.close()
    w.close()


def make_raw_list(scam_data_file, enron_data_file, scam_mail_file, enron_mail_file):
    scam_dic = counting_keyword(scam_data_file)
    enron_dic = counting_keyword(enron_data_file)
    words = count_combine(scam_dic, enron_dic)
    mail = count_combine(counting_mail(scam_mail_file),counting_mail(enron_mail_file))
    result = tfidf(scam_dic, enron_dic, words, mail)
    store_file = write_blacklist(0,result,scam_dic, enron_dic)
    return store_file

#file = make_raw_list("keywordscam_no_num.txt", "keywordenron_no_num.txt", "result_scam_keyword1129.txt","result_keyword1129.txt")
file = "tfdif_0.005_558.txt"
expand_wordlist(file)
