# Extract keywords
> DetectPhishingMail.java



### External JARs
```
commons-lang-2.6.jar
edu.mit.jwi_2.4.0.jar
gson-2.8.0.jar
stanford-corenlp-3.8.0.jar
stanford-english-corenlp-2017-06-09-models.jar
stanford-parser.jar
```

# TFIDF & Data expansion
> lesk.py

## Python version
3.6

## TFIDF
```
python lesk.py <scam data file> <enron data file> <scam mail file> <enron data file>
```
* input
scam data file, enron data file : text file with every keywords in whole mail 
scam_mail_file, enron_mail_file : text file with every keywords in whole mail but non-overlapping keywords in the one mail
* output
  sorted keywords file by TFIDF

## Data expansion
```
python lesk.py <input file name>
```
Add synonym words using lesk algorithm and refine words

* input, output
keywords file [verb obj\n]
