# Sentence Boundary Detection

We provide pre-sentence-tokenized emails. You can use our pre-sentence-tokenized email data in [google drive](https://drive.google.com/file/d/1tveWU5yungDuWlnBhlkfhkNM8CW21Xxw/view?usp=sharing).  
If you want to tokenize sentences by yourself, you can use our code.

## Puncatuate

### Motivaiton

Some of scam emails miss their punctuations, so it is hard to detect sentence boundary. We use punctuator for adding punctuations. You can see this algorithm on this [paper](http://www.isca-speech.org/archive/Interspeech_2016/pdfs/1517.PDF) and code [here](https://github.com/ottokart/punctuator2).  

### Idea

Punctuator2 can train model twice.  

1. Bidirectional GRU model with attention-mechanism, and late-fusion.
2. With late fusion output, add pause-duration and adapt to target domain, the second stage discards the first stage output layer and replaces it with a new recurrent GRU layer.

I used pre-trained model that they provide : [click](https://drive.google.com/drive/folders/0B7BsN5f2F1fZQnFsbzJ3TWxxMms)

### Code Description

Our code is on [github](https://github.com/zerobugplz/social-engineering-defense/tree/sentence_boundary_detection/sentence_boundary_detection/punctuator2-1.0). You should download [pre-training dataset](https://drive.google.com/drive/folders/0B7BsN5f2F1fZQnFsbzJ3TWxxMms) to run punctuator. Then just run [play_with_model.py](https://github.com/zerobugplz/social-engineering-defense/blob/sentence_boundary_detection/sentence_boundary_detection/punctuator2-1.0/play_with_model.py) with your text dataset. Because I use scam emails, and it has already punctuations partially, I change this punctuator2 code to punctuate only if there is no period or question mark nearby.  

### Training Details

- Adagrad optimizer : learning rate 0.02
- L2-norm < 2
- 5 epochs early termination
- Weight : normalizer initialization
- Hidden layer 256
- Activation function : tanh
- Mini-batches : 128
- Trained by Theano framework
- Data : [INTERSPEECH-T-BRNN-pre.pcl](https://drive.google.com/drive/folders/0B7BsN5f2F1fZQnFsbzJ3TWxxMms)

## Sentence Tokenizing with Punkt(NLTK).

### Motivation

It is one of the most popular sentence tokenizer algorithm in the world. It performs well and easy to use.

### Idea

![punkt_structure](https://github.com/learnitdeep/social-engineering-defense/blob/master/punkt_structure.png)  

- S : Sentence Boundary
- A : Abbreviation
- E : Ellipsis
- AS : Abbreviation at the End of Sentence
- ES : Ellipsis at the End of Sentence

### Code Description

Punkt algorithm has dependency with space between sentence and sentece because if sentences are not separated by period+spaces, but only with period, the algorithm detects it as an abbreviation in many cases. So I make a space when period is not used for abbreviation('Mr.', 'Ms.', 'Mrs.', 'www.', '@', 'Dr.', 'mr.', 'mrs.', 'dr.', 'Www.', 'http', 'Co.', and 'co.'). Just type below commands on your terminal.

```
pip install nltk
python punkt.py
```
