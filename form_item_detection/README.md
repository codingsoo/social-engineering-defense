# Form Item Detection

## get_rid_of_header/getRidOfHeader.py

1. Input = email text that contain html tags  
2. Output = email content text List (each element of list is a one email content data)
```
python2.7 get_rid_of_header/getRidOfHeader.py [input_file_path/file_name][output_file_path/file_name]
```

## get_rid_of_header/Form_more_than_two_line.py

1. Input = email content text List (each element of list is a one email content data)
2. Output = question List (2d List of question, e.g. [[question 1, question 2, …] , [question1, …]])
```
python2.7 get_rid_of_header/Form_more_than_two_line.py [input_file_path/file_name][output_file_path/file_name]
```
- Please double check stanfod parser path before run this code.
- We recommend to use stanford parser version 3.8

## check_form/check_q_in_db.py

We use modified PARALEX (that contain only private data), and PARALEX web evaluation mode to check whether the question is private or not.

1. Input = question List (2d List of question, e.g. [[question 1, question 2, …] , [question1, …]])
2. Output = result of check (1d List of private[true/false])

```
python2.7 check_form/check_q_in_db.py  [input_file_path/file_name][output_file_path/file_name]
```
