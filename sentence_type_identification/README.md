# Sentence Type Identification
> DetectQuestionImperative.java
## Command Detection

We present four different types of commands, each of which is identified in a different way.

#### 1. Direct Imperative
We used a tregex pattern to extract the direct imperative sentence.

```
((@VP=verb > (S !> SBAR)) !$,,@NP)
```

| Expression | Explanation |
| ------ | ------ |
| A $, , B | A is a sister of B and follows B|
| A > B | A is immediately dominated by B|
| SBAR | Subordinate Clause |
| @ | the label will match any node whose basic Category matches the description. |

#### 2. Polite Prefix
Polite prefix imperative can also extract by using tregex pattern. Because most of polite prefix imperatives start with the words 'Please', 'Kindly', and so on. 

#### 3. Suggestion
In order to detect the suggestion expression, We made a list of PennTreebank tags with words. For example, There is a sentence.
```
You must send me your password.
```

After tagging,

```
["You/PRP", "must/MD", "send/VB", "me/PRP", "your/PRP$", "password/NN", "./."]
```

If there is a modal verb(MD) such as 'must', 'should', or 'could', we check in front of the modal verb. If there is a word 'you', the method returns 'true'.

#### 4. Expression of Desire
Another way to soften a command is to prefix it with an expression of desire. In order to detect the desire expression, we used a type dependency list. Stanford dependencies provide a representation of grammatical relations between words in a sentence.
The detection of commands which express desire is performed based on two observations.
* The sentence must include a desire verb such as 'want' or 'hope'.
* the pronoun 'you' is the direct object of the desire verb.
For example, There is a sentence.
```
I want you to come in.
```
This sentence has dependencies below.
```
nsubj(want-2, I-1)
root(ROOT-0, want-2)
dobj(want-2, you-3)
mark(come-5, to-4)
xcomp(want-2, come-5)
compound:prt(come-5, in-6)
```
If the sentence has a desire verb and pronoun 'you' in 'dobj' dependency, we extract the sentence.

## Question Detection
Question detection is straightforward using the syntactic parse tree of the sentence. There are two types of questions. 


### Development environment
* Windows 10
* Eclipse Oxygen.2