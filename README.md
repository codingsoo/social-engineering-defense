# Social Engineering Defense

## Purpose

We present an approach which analyzes attack content to detect inappropriate statements which are indicative of social engineering attacks.

## System Structure

![system_structure](https://github.com/learnitdeep/social-engineering-defense/blob/master/system_structure.png)  

### Data

We use email data, but input can be any text-data. You can crawl email data in [crawl_mails folder](https://github.com/zerobugplz/social-engineering-defense/tree/master/crawl_mails), or you can use [pre-crawled email data](https://drive.google.com/file/d/1D8BUS_wxZVip6EFmhMkrXunBXcuBev7o/view?usp=sharing).

### Sentence Processing

The text is processed by partitioning it into sentences and parsing each sentence to gain structural information which will be used for analysis. Separating text into sentences is performed by using the Punctuator tool to insert periods at appropriate locations. The Punkt tool partitions sentences at the period boundaries, differentiating between periods which end sentences and those which are part of abbreviations. You can see details in [sentence_boundary_detection folder](https://github.com/zerobugplz/social-engineering-defense/tree/master/sentence_boundary_detection), or you can use [pre-sentence-tokenized email data](https://drive.google.com/file/d/1tveWU5yungDuWlnBhlkfhkNM8CW21Xxw/view?usp=sharing).

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
Questions and commands are the types of sentences which are of interest because they might refer to private data or private operations. The identification of questions and commands analyzes the words in the sentence and the syntactic and typed dependency parse trees of the sentence which were created in the Sentence Processing step. You can see the detail in [check phishing with command folder](https://github.com/zerobugplz/social-engineering-defense/tree/master/detectQuestionCommand).

### Command and Question Analysis

We extract the command in four cases below.
#### 1. Imperative
This is to find the imperative sentences which generally start with the verb. 
```
Send me money!
Let me know your information.
```
#### 2. Suggestion
If there is a ‘you’ in front of the modal verb.
```
You should send me your address.
You must call him.
```
#### 3. DesireExpression
If the verb is included in desire verb
```
I hope you will give me the money.
I want you to do these things.
```
#### 4. Question
If there are ‘SQ’ tag or ‘SBARQ’ tag in parse result

### Check Malicious with Blacklist

We use blacklist for checking whether it's scam or not
```
transport money
ship money
send money
notify we
```
You can see the detail in [check phishing with command folder](https://github.com/zerobugplz/social-engineering-defense/blob/master/check_phishing_with_command)
