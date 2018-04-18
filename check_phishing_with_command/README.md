# 1. Extract keywords
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

### Keywords extraction
```
javac -cp <jar file path> <java file and location>
max  ex) javac -cp D:dev\jar\*: DetectPhishingMail.java CoreNLP.java MakeBlacklist.java WordNet.java
window ex) javac -cp D:dev\jar\*; DetectPhishingMail.java CoreNLP.java MakeBlacklist.java WordNet.java

```

```
java -cp <jar file path> <input_file> <output_file>
mac ex) java -cp D:dev\jar\*: DetectPhishingMail "input.txt" "null"
window ex) java -cp D:dev\jar\*; DetectPhishingMail "input.txt" "null"
```
* input_file : json or text or null(input)
* output_file : text file name or null (write or not) 
              [verb obj\n ...]

# 2. TFIDF & Data expansion for making blacklist
> lesk.py

## Python version
3.6

## TFIDF(Term Frequency - Inverse Document Frequency)
TF : In the case of the term frequency tf(t,d), the simplest choice is to use the raw count of a term in a document, i.e. the number of times that term t occurs in document d. (referenced by WiKipedia)
IDF : The inverse document frequency is a measure of how much information the word provides, that is, whether the term is common or rare across all documents. It is the logarithmically scaled inverse fraction of the documents that contain the word, obtained by dividing the total number of documents by the number of documents containing the term, and then taking the logarithm of that quotient.(referenced by WiKipedia)
```
d : scam mail data
N : number of mail
|{d ∈ D: t ∈ d}|  : number of documents where the term t appears
tf = log(1 + frequency(t, d)) 
idf = log(N/|{d ∈ D: t ∈ d}|)
```
we use high score 0.45% keywords

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

# 3. Check malicious sentence
> DetectPhishingMail.java

```
java -cp <jar list and location> <blacklist file> <keywords file> <input_file> <output_file>
mac : java -cp  <jar file path>\*: DetectPhishingMail "result.txt" "null" "null" "temp.txt"
window : java -cp  <jar file path>\*; DetectPhishingMail "result.txt" "null" "null" "temp.txt"
```
blacklist file : blacklist name("result.txt")
keywords file : verb+obj File("data.txt") or null(using blacklist)
input file : json or text or null(keyboard input)
output file : text file name or null (write or not)
            [1,0,1,1....] (malicious or not)

# 4. Calculate Accuracy
Extract 100000 mail from scam_data.json
we got imperative sentences from extracted 100000 mail each scam and non-scam
we got verb & obj sets from imperative sentences.
After sorting by the TF-IDF score, we delete the keywords under 4.0 score.
expand synonym words, keywords increase from 874 to 2952 

#### scam data
total : 87049
detect : 59724
fail to detect : 27325
#### enron data
total : 87049
detect : 18539
fail to detect : 68510

* Precision : 76.3%	
* Recall : 68.6%
