from nltk.wsd import lesk
from nltk import sent_tokenize
from nltk.corpus import wordnet as wn


r = open("data_origin.txt","r")
w = open("data.txt", "w")

def func(word, sent, list):
    lesk_w = lesk(sent, word)
    if (lesk_w):
        temp = lesk_w.lemma_names()
        for t in temp:
            if(len(t.split("_")) > 1):
                temp.remove(t)
        return temp
    return ""

def writer(verb, obj):
    for v in verb:
        for o in obj:
            if o and v:
                w.write(v + ' ' + o + "\n")

for sent in r.readlines():
    verb = []
    obj = []
    v,b = sent.split(" ")[0], sent.split(" ")[1]
    b = b.split("\n")[0]
    verb.append(v)
    obj.append(b)
    verb += func(v,sent,verb)
    obj += func(b,sent,obj)

    writer(set(verb), set(obj))