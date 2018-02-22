For form detecting.

1. 
	Input = email text that contain html tags<br>
	Output = email content text List (each element of list is a one email content data)

	    python2.7 get_rid_of_header/getRidOfHeader.py [input_file_path/file_name][output_file_path/file_name]

2.   Input = email content text List (each element of list is a one email content data) <br>
    Output = question List (2d List of question, e.g. [[question 1, question 2, …] , [question1, …]]) <br>
    
        get_rid_of_header/Form_more_than_two_line.py [input_file_path/file_name][output_file_path/file_name]
    
    -  Be sure about if there are module at the correct path and you have before run it.
    - stanford-parser version. *   (Problem with version when using “corenlp.py”) <br>
        ##### recommend to "use PARALEX’s corenlp version"  ####
    -  stanford-parser path.       
    - Stanford-parser models path.        
    - nltk download status  python2.7 
    
    

3.  use modified PARALEX (that contain only private data) and  using PARALEX web evaluation mode to check the question is private or not. <br>
    Input = question List (2d List of question, e.g. [[question 1, question 2, …] , [question1, …]]) <br>
    Output = result of check (1d List of private[true/false])  
    
        python2.7 check_form/check_q_in_db.py  [input_file_path/file_name][output_file_path/file_name]
