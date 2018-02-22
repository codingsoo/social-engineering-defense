# coding: utf-8

from __future__ import division
import json
import models
import data

import theano
import sys
import codecs

import theano.tensor as T
import numpy as np

def to_array(arr, dtype=np.int32):
    # minibatch of 1 sequence as column
    return np.array([arr], dtype=dtype).T

def convert_punctuation_to_readable(punct_token):
    if punct_token == data.SPACE:
        return " "
    else:
        return punct_token[0]

def punctuate(predict, word_vocabulary, punctuation_vocabulary, reverse_punctuation_vocabulary, text):
    text = [w for w in text.split() if w not in punctuation_vocabulary] + [data.END]

    i = 0
    punctuated_text = ''

    while True:

        subsequence = text[i:i+data.MAX_SEQUENCE_LEN]

        if len(subsequence) == 0:
            break

        converted_subsequence = [word_vocabulary.get(w, word_vocabulary[data.UNK]) for w in subsequence]

        y = predict(to_array(converted_subsequence))

        punctuated_text = punctuated_text + subsequence[0]

        last_eos_idx = 0
        punctuations = []
        for y_t in y:

            p_i = np.argmax(y_t.flatten())
            punctuation = reverse_punctuation_vocabulary[p_i]

            punctuations.append(punctuation)

            if punctuation in data.EOS_TOKENS:
                last_eos_idx = len(punctuations) # we intentionally want the index of next element

        if subsequence[-1] == data.END:
            step = len(subsequence) - 1
        elif last_eos_idx != 0:
            step = last_eos_idx
        else:
            step = len(subsequence) - 1

        for j in range(step):
            punctuated_text = punctuated_text + (" " + punctuations[j] + " " if punctuations[j] != data.SPACE else " ")
            if j < step - 1:
                punctuated_text = punctuated_text + (subsequence[1+j])

        if subsequence[-1] == data.END:
            break

        i += step

    return punctuated_text



model_file = 'INTERSPEECH-T-BRNN-pre.pcl'

x = T.imatrix('x')

print "Loading model parameters..."
net, _ = models.load(model_file, 1, x)

print "Building model..."
predict = theano.function(inputs=[x], outputs=net.y)
word_vocabulary = net.x_vocabulary
punctuation_vocabulary = net.y_vocabulary
reverse_punctuation_vocabulary = {v:k for k,v in net.y_vocabulary.items()}

punctuated_scam_email = []
count = 0

with open('scam_email_body.json','r') as f:
    scam_email = json.load(f)
    for text in scam_email:
        count = count + 1
        print(count)
        try:
            punctuated_email = ''
            text = text.replace('\n', ' ')
            text = text.replace('\t', ' ')
            text = text.replace("&", "and")
            text = text.replace(";", "")
            punctuated_scam = str(punctuate(predict, word_vocabulary, punctuation_vocabulary, reverse_punctuation_vocabulary, text))
            punctuated_scam = punctuated_scam.replace(",COMMA ","")
            punctuated_scam_email.append(punctuated_scam)

        except:
            print(Exception)

with open('punctuated_scam17.json','w') as f:
    json.dump(punctuated_scam_email,f)
