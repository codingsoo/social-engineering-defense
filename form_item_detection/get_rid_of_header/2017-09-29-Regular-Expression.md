---
layout: post
cover: false
title: Regular Expression
date:   2017-09-29 16:25:00
tags: study
navigation : true
subclass: 'post tag-study'
categories: 'casper'
---


#Regex (Regular Expression)

A regular expression(regex, regexp) is a sequence of characters that define a search pattern.<br>
Usually this pattern is then used by string searching algorithms for **"find"** or **"find and replace"** operations on strings.
<br><br>
정규표현식은 특정한 규칙을 가진 문자열의 집합을 표현하는데 사용하는 언어이다. <br>
주로 텍스트 편집기와 프로그래밍 언어에서 **문자열 검색**과 **치환**을 위해 사용이 된다  


**Boolean "or"**<br>

A vertical bar separates alternatives.<br>
'|'(수직 바, 'shift + \')는 "or"로 둘다 포함하는 "또는"의 의미로 이용한다.
     
    gray|grey can match "gray" or "grey".
        
    gray|grey 는 "gray"나 "grey"에 매치될 수 있다.

**Grouping**
    
Parentheses are used to define the scope and precedence of the operators (among other uses). <br>
'( )'(괄호)는 그룹화에 이용한다

    gray|grey and gr(a|e)y are equivalent patterns which both describe the set of "gray" or "grey".
        
    gray|grey 와 gr(a|e)y는 a와 e를 그룹화하여 해당 자리에 어떤 값이 연결될 수 있는지 결정할 수 있게 한다. 따라서, "gray"나 "grey"에 매치될 수 있다.

**Pattern**

The patterns are used to match other things.<br>
여러가지 패턴을 이용하여 정규표현식을 구성할 수 있다
    
    ?	            The question mark indicates zero or one occurrences of the preceding element. 
                    For example, colou?r matches both "color" and "colour".
                    
                    '?'는 0 ~ 1 회 사용을 의미하여, 해당 자리에 해당 문자가 존재하거나 존재하지 않거나 두 경우 모두 처리할때 이용한다.
                    예를 들어 "colou?r" 인경우, 존재하지 않는 "color"와  존재하는 경우인 "colour"가 매칭될 수 있다
                    
    *	            The asterisk indicates zero or more occurrences of the preceding element. 
                    For example, ab*c matches "ac", "abc", "abbc", "abbbc", and so on.
                    
                    '*'는 0 ~ 여러번 사용을 의미하여, 해당 자리의 문자 또는 문자열이 반복 사용되는 경우를 처리할 때 이용한다.
                    예를들어 "ab*c" 인 경우, 사용되지 않은 "ac"와 사용되는 "abc", "abbc", "abbc" 등의 경우 매칭된다.
                    
    +	            The plus sign indicates one or more occurrences of the preceding element. 
                    For example, ab+c matches "abc", "abbc", "abbbc", and so on, but not "ac".
                    
                    '+' 는 1 ~ 여러번 사용을 의미하며, 해당 자리의 문자 또는 문자열이 한번 이상 사용되는 경우를 처리할 때 이용한다.
                    예를 들어 "ab+c" 인 경우, "abc", "abbc", "abbbc" 등의 경우 매칭된다. 
                    
    {n}           The preceding item is matched exactly n times.
            
                  정확히 n번 만큼 일치시킨다.  
                    
    {min,}        The preceding item is matched min or more times.
        
                  min번 이상 만큼 일치시킨다.
                    
    {min,max}	    The preceding item is matched at least min times, but not more than max times.
                        
                    min번 이상 max번 미만 만큼 일치시킨다.
                    
    \n              The iten is matched order of exact number. 
        
                    일치하는 패턴들 중 n(1-9)번째를 선택할 때 이용한다.
                    
    .               The period mark matches any single character(exclude newline).
        
                    '.' 은 개행을 제외한 모든 문자의 매칭에 사용된다.
                    
    [ ]            A bracket expression. Matches a single character that is contained within the brackets.
                   Then, '[^ ]' matches except behind of '^' mark.
                    For example, [a-zA-Z] matches 'a' to 'z' and 'A' to 'Z'.
                    [^hc]at matches except 'hat' and 'cat'.
                    
                  '[ ]' 는 괄호안의 문자들에 범위 매칭에 이용한다. 또한 '[^ ]'의 경우 해당 괄호 안의 문자의 범위를 제외한 결과를 나타낸다.
                   예를 들어 [a-zA-Z]는 알파벳 소문자 'a' 부터 'z' 까지와 알파벳 대문자 'A' 부터 'Z'까지를 포함하며,
                   '[^hc]at' 는 'hat'과 'cat'을 제외한 모둔 ' at'를 찾는것에 이용한다.  
    
Advanced,

    \b                  Matches a zero-width boundary between a word-class character (see next) and either a non-word class character or an edge; 
                        same as (^\w|\w$|\W\w|\w\W).
                            
                        
                        
    \w                  Matches an alphanumeric character, including "_";
                        same as [A-Za-z0-9_].
                            
                        "_"를 포함한 영숫자를 일치시킨다.
                        
    \W                  Matches a non-alphanumeric character, excluding "_";
                        same as [^A-Za-z0-9_]
                        
                        "_"를 제외하여 영숫자가 아닌 문자열들과 일치시킨다.
        
    \s                  Matches a whitespace character,
                        like tab, line feed, form feed, carriage return, and space;
        
                        공백 문자와 일치시킨다.
                                    
    \S                  Matches anythings but a whitespace.
        
                        공백을 제외한 항목을 일치시킨다.
        
    \d                  Matches a digit;
                        same as [0-9]
        
                        숫자를 일치시킨다.
                            
    \D                  Matches a non-digit;
                        same as [^0-9]
                        
                        숫자가 아닌 항목을 일치시킨다
                        
    ^                   Matches the beginning of a line or string.
                        
                        줄이나 문자열의 시작점과 일치시킨다. (문자열이 여려줄 일 경우 각 줄의 처음을 확인)
        
    $                   Matches the end of a line or string.
        
                        줄이나 문자열의 끝과 일치시킨다. (문자열이 여려줄 일 경우 각 줄의 처음을 확인) 
                        
    \A                  Macthes the beginning of a string.
                        
                        줄이 아니라 문자열의 처음 시작점과 일치시킨다.
                        
    \z                  Macthes the end of a string.
        
                        줄이 아니라 문자열의 끝과 일치시킨다.                

e.g.                
    
    a|b*                denotes {ε, "a", "b", "bb", "bbb", …}
        
    (a|b)*              denotes the set of all strings with no symbols other than "a" and "b", including the empty string: {ε, "a", "b", "aa", "ab", "ba", "bb", "aaa", …}
        
    ab*(c|ε)            denotes the set of strings starting with "a", then zero or more "b"s and finally optionally a "c": {"a", "ac", "ab", "abc", "abb", "abbc", …}
        
    (0|(1(01*0)*1))*    denotes the set of binary numbers that are multiples of 3: { ε, "0", "00", "11", "000", "011", "110", "0000", "0011", "0110", "1001", "1100", "1111", "00000", … }
    
  