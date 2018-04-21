# Social Engineering Defense

## Purpose

The purpose of this project is to detect scam e-mails based on text analysis. This repository includes e-mail crawling, sentence processing, sentence type identification, form item detection, command analysis and question analysis. We used open-source projects for each steps as follows.

- Crawling
 - Beautifulsoup for crawling data
 - Langid for selecting English data
- Sentence Processing
 - Punctuator2 for punctuating
 - Punkt Algorithm(nltk) for sentence tokenizing
- Sentence Type Identification
 - Corenlp for finding word dependency tree
 - Stanford Parser for POS tagging
- Question Analysis
 - Paralex for detecting question scams.
- Command Analysis
 - edu.mit.jwi_2.4.0.jar for using wordnet
 - gson-2.8.0.jar for using json
 - commons-lang-2.6.jar for capitalization
 - Corenlp for finding word dependency tree
 - Stanford Parser for POS tagging
 
The structure of our social engineering defense system is as shown in the following figure.

## System Structure

![system_structure](https://github.com/zerobugplz/social-engineering-defense/blob/master/system_structure.png)  

### Data

We use email data, but input can be any text-data. You can crawl email data in [crawl_mails folder](https://github.com/zerobugplz/social-engineering-defense/tree/master/crawl_mails), or you can use [pre-crawled email data](https://drive.google.com/file/d/1D8BUS_wxZVip6EFmhMkrXunBXcuBev7o/view?usp=sharing). You can see detail code in [crawl_mails folder](https://github.com/zerobugplz/social-engineering-defense/tree/master/crawl_mails)

### Sentence Processing

Analyzing scam data is hard because they destroy grammar on purpose. The most critical point is they don't have right punctuations. It is almost impossible to break down into sentences without punctuations. We used [punctuator2](https://github.com/ottokart/punctuator2) for solving this problem. Then we detect sentence boundary with Punkt tool. It finds periods, but can distinguish whether it is used for ending sentences or abbreviations. You can see details in [sentence_boundary_detection folder](https://github.com/zerobugplz/social-engineering-defense/tree/master/sentence_boundary_detection). You can use [pre-sentence-tokenized email data](https://drive.google.com/open?id=1_LHcOVE0A3hd1mBwZl-o4ivSfu42IGJV).

### Form Item Detection

If your data has no form, you can skip this section. However, if you use our data, or something with form, you should transform form to question.  
```
please fill this form
NAME : ___________________
JOB : ___________________
PHONE : _________________
```
This form can change to below questions.
```
what is your name?
what is your job?
what is your phone?
```

You can see details in [form_item_detection folder](https://github.com/zerobugplz/social-engineering-defense/tree/master/form_item_detection).

### Sentence Type Identification

Scammers get information through questions or commands. Since we provide different approach for these two categories, we used POS tagger which is provided from stanford(coreNLP) to identifying them. This tagger draws the syntactic and type dependency parse trees of sentences. We extract the commands in four cases below.

#### 1. Imperative
We find the imperative sentences which generally start with the verb. 
```
Send me money!
Let me know your information.
```
#### 2. Suggestion
If there is a ‘you’ in front of the modal verb, we detect it as command.
```
You should send me your address.
You must call him.
```
#### 3. DesireExpression
If the verb is included in desire verb, we detect it as command.
```
I hope you will give me the money.
I want you to do these things.
```
#### 4. Question
If there are ‘SQ’ tag or ‘SBARQ’ tag in parse result, we detect is as question.  
You can see the details in [sentence type identification folder](https://github.com/zerobugplz/social-engineering-defense/tree/master/sentence_type_identification).

### Check Malicious with Blacklist

We use blacklist for checking whether it's scam or not.
```
transport money
ship money
send money
notify we
```
You can see the detail in [check phishing with command folder](https://github.com/zerobugplz/social-engineering-defense/blob/master/check_phishing_with_command)

### Check Malicious with Question Answering System(Paralex)

We use [paralex](http://knowitall.cs.washington.edu/paralex/) system for this step. Please download our [file](https://drive.google.com/file/d/1XYXagUwkcKcFUU6Kljvh6zJAVSnHnM0t/view?usp=drive_web).  

```
unzip paralex-evaluation-test.zip
cd paralex-evaluation-test
./scripts/start_nlp.sh & # start nlp server
./scripts/start_demo.sh & # start demo server
```

Once the demo is running, you can make HTTP requests to Paralex and get JSON objects as output. 

```
curl http://localhost:8083/parse?sent=What+is+your+password # "answers": ["confidential.e"]
curl http://localhost:8083/parse?sent=Who+invented+pizza # have no ["confidential.e"] answers
```


## Demo

We provide docker demo. Our system originally can handle files as an input, but demo only provides sentence. You can download docker image on dockerhub. Make sure that login dockerhub before pull our image.
```
docker login
docker pull learnitdeep/social-engineering-defense
```

Please download our [file](https://drive.google.com/open?id=1AFKGLJj_JQnPhbi42SCzQdMrTWIKJPz6), and unzip it. you can link your computer's directory and docker containers directory with -v option.  
  
This is the example.
```
docker run -it -v /Users/learnitdeep/Desktop/social-engineering-defense-v1.0/:/workdir learnitdeep/social-engineering-defense
```

Then you need to run paralex server.  
```
cd /workdir/social-engineering-defense/paralex-evaluation-test/
./scripts/start_nlp.sh & # start nlp server
./scripts/start_demo.sh & # start demo server
```

Our demo file is located in /check_phishing_with_command/demo.py.
First, you need to compile java files.
```
cd /workdir/social-engineering-defense/check_phishing_with_command
javac -cp "./jar/*" DetectPhishingMail.java CoreNLP.java MakeBlacklist.java WordNet.java
```

Now you can use our demo!

```
python demo.py what is your password # Beep! Scam detected.
```

## Ongoing work

- Make punctuator faster(using different model)
- Collect other text data apart from emails
- improve form detecting algorithm
