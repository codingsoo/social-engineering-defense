# Extract keywords
> DetectPhishingMail.java

### External JARs
```
edu.mit.jwi_2.4.0.jar
gson-2.8.0.jar
stanford-corenlp-3.8.0.jar
stanford-english-corenlp-2017-06-09-models.jar
stanford-parser.jar
commons-lang-2.6.jar
```

##### JWI
JWI is a Java library for interfacing with Wordnet.
[here](https://projects.csail.mit.edu/jwi/)
We used it for verifying synonyms. 

##### GSON
Gson is a Java library that can be used to convert Java Objects into their JSON representation.
[here](https://mvnrepository.com/artifact/com.google.code.gson/gson/2.8.0)

##### Stanford corenlp
Stanford CoreNLP provides a set of human language technology tools.
[here](https://stanfordnlp.github.io/CoreNLP/download.html)

##### Stanford parser
Java implementation of probabilistic natural language parsers.
[here](https://nlp.stanford.edu/software/lex-parser.shtml#Download)

### Dictionary
We used wordnet 3.0 dictionary [here](http://wordnet.princeton.edu/wordnet/download/current-version/)

```
javac -classpath "[jarname with specified path]" [java filename]
```

### Keywords extraction
```
java -cp <jar list> <input_file> <output_file>
```
* input_file : json or text or null(input)
* output_file : text file name or null (write or not) 
              [verb obj\n ...]

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

* input, output : [verb obj\n]

### Check malicious sentence
```
java -cp <jar list> <blacklist file> <keywords file> <input_file> <output_file>
```
blacklist file : blacklist name
keywords file : verb+obj File(make blacklist) or null(using blacklist)
input file : json or text or null(input)
output file : text file name or null (write or not)
            [1,0,1,1....] (malicious or not)

---
### Development evironment
* Window 10
* Eclipse Oxygen.2
* Pycharm professional
