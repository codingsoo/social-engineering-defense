# Crawling

We provide pre-crawled emails. You can use our pre-crawled-email data in [google drive](https://drive.google.com/file/d/1D8BUS_wxZVip6EFmhMkrXunBXcuBev7o/view?usp=sharing).  
If you want to crawl emails by yourself, you can use our code.

## Scam Crawling

We crawl scam datas from three websites.  

1. scamalot_com.py => http://www.scamalot.com/ScamTipReports/
2. antifraudintl_org.py => http://antifraudintl.org/
3. scamwarners_com.py => https://www.scamwarners.com/forum/
4. scamdex.py => http://www.scamdex.com/

Just type python ****.py on your computer!  

### Scamdex

We crawl scam email [here](http://www.scamdex.com) from 2007.10.16 to 2017.10.11. Some of them were not English and empty, so I parsed them with ${langid}^{[1]}$ and only for more then 10 characters.

- Total number of scam emails : 56555
- Total number of images : 8423

Most of images are not for the scam, but for the their fake logo.
crawling source code is [here](https://github.com/zerobugplz/social-engineering-defense/blob/master/crawling_scam_mails/scamdex.py).

1. [langid](https://github.com/saffsd/langid.py) is an accurate language distinguish library based on text data. more details are on their [paper](http://www.aclweb.org/anthology/P12-3005)

### Scamwarners

We crawl scam emails [here](http://www.scamwarners.com) from beginning to 2017.10.11. Since it is a community sites, there were some questions about scams, and giving information about scams. To avoid them, we crawled only texts which have simple email form ("From:").

- Total number of scam emails : 43241
- Total number of images : 471

crawling source code is [here](https://github.com/zerobugplz/social-engineering-defense/blob/master/crawling_scam_mails/scamwarners_com.py).

### Scamalot

We crawled scam emails [here](https://scamalot.com) from 2011.07.30 to 2017.10.11. It has also questions, information, same reason as Scamwarners. So that we crawled only texts which have scammer's email address.

- Total number of scam emails : 18149
- Total number of images : 69

crawling source code is [here](https://github.com/zerobugplz/social-engineering-defense/blob/master/crawling_scam_mails/scamalot_com.py).

### Antifraudintl

We crawled scam email [here](http://antifraudintl.org) from 2007.02.01 to 2017.10.12. We apply same algorithm as scamwarners.

- Total number of scam emails : 69209
- Total number of images : 754

crawling source code is [here](https://github.com/zerobugplz/social-engineering-defense/blob/master/crawling_scam_mails/antifraudintl_org.py).

### Total

Total number of scam email data is 187154.

### remove header

We removed header to parse only email body beacuse our approach is only for natural language. source code is [here](https://github.com/zerobugplz/social-engineering-defense/blob/master/crawling_scam_mails/remove_header.py)

### Python version

2.7.10

## Non Scam Crawling

### Enron Email

We use Enron email as non-scam-data which contains data from about 150 users, mostly senior management of Enron. You can get more details about Enron email data [here](https://www.cs.cmu.edu/~enron/). I randomly chose 187048 enron data because we have 187048 scam emails(some were empty out of 187154). You can download through this [link](https://drive.google.com/file/d/1huRLrUc7G1GdEfUb2t2rwAFoI9xlc3Wm/view?usp=sharing).
