#scam datasets

## crawling

### Crawling web sites
---
We crawled scam datas from three websites.  

1. http://www.scamalot.com/ScamTipReports/
2. http://antifraudintl.org/
3. https://www.scamwarners.com/forum

### Crawled datas result
---

result is on [my blog](https://zerobugplz.github.io/scam-mail-crawling.html).  
Total number of data is 141607.   
You have options for choose seperate datas like(1,3,7,10) or continuous datas like(10-30).

### Python version

2.7.10

### Punctuation

Some of scam mails missed their punctuations. Most of "detecting sentence boundary algorithms" perform better with appropriate punctuation. We use Punctuator(1) : bidirectional recurrent neural network model with attention mechanism. It can be trained twice with text file and audio file. Text file should have clear period, comma, and question mark. Audio file should be re-writed to text file with silence time. If silence time is long, it judges as independent senteces and put period. If it is just a little time, put comma. If it doesn't have silence time, put nothing. Punctuator was trained with Europarl v7 monolingual English corpus(text file), and IWSLT 2012 TED task(audio file) for 256 hidden layer, 0.02 learning rate, and 5 epochs. Trained punctuator's input is text-file and output is punctuated text file(commas, periods, question marks, exclamation marks, colons, semicolons and dashes). You can see more detail at annotation \[1\].  

1. Bidirectional Recurrent Neural Network with Attention Mechanism : Ottokar Tilk, Tanel Alumae, "Bidirectional Recurrent Neural Network with Attention Mechanism for Punctuation Restoration" INTERSPEECH 2016 September 8–12, 2016. San Francisco, USA
