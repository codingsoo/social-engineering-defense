import subprocess 
import sys

sentence1 = sys.argv[1]
sentence2 = sys.argv[1]

for count,word in enumerate(sys.argv):
    if(count>1):
        sentence1 = sentence1 + '+' + word
        sentence2 = sentence2 + ' ' + word

o1 = 'curl http://localhost:8083/parse?sent='+str(sentence1)
o2 = 'java -cp “./jar/*:” DetectPhishingMail "result.txt" "null" "null" "temp.txt" '+str(sentence2)

result1 = subprocess.check_output(o1, shell=True)
result2 = subprocess.check_output(o2, shell=True)

if ('confidential' in result1) or ('true' in result2): print "Beep! Scam detected."
else: print "Normal sentence"
