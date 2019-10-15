<?php

    preg_match(<error descr="[EA] ( \d* )* might be exploited (ReDoS, Regular Expression Denial of Service).">'/(\d*)*/'</error>, '');
    preg_match(<error descr="[EA] ( \D+ )+ might be exploited (ReDoS, Regular Expression Denial of Service).">'/(\D+)+/'</error>, '');
    preg_match(<error descr="[EA] ( \w* )+ might be exploited (ReDoS, Regular Expression Denial of Service).">'/(\w*)+/'</error>, '');
    preg_match(<error descr="[EA] ( \W+ )* might be exploited (ReDoS, Regular Expression Denial of Service).">'/(\W+)*/'</error>, '');
    preg_match(<error descr="[EA] ( \s* )* might be exploited (ReDoS, Regular Expression Denial of Service).">'/(\s*)*/'</error>, '');
    preg_match(<error descr="[EA] ( \S* )* might be exploited (ReDoS, Regular Expression Denial of Service).">'/(\S*)*/'</error>, '');

    preg_match(<error descr="[EA] ( \D* )* might be exploited (ReDoS, Regular Expression Denial of Service).">'/(?:\D*)*/'</error>, '');
    preg_match(<error descr="[EA] ( \D* )* might be exploited (ReDoS, Regular Expression Denial of Service).">'/(\D*|0(?!1))*/'</error>, '');
    preg_match(<error descr="[EA] ( \D* )* might be exploited (ReDoS, Regular Expression Denial of Service).">'/(\D*|)*/'</error>, '');
    preg_match(<error descr="[EA] ( \D* )* might be exploited (ReDoS, Regular Expression Denial of Service).">'/(|\D*|)*/'</error>, '');
    preg_match(<error descr="[EA] ( \D* )* might be exploited (ReDoS, Regular Expression Denial of Service).">'/(|\D*)*/'</error>, '');

    preg_match('/(\D*){1,10}/', '');
    preg_match('/(\D*){1,}/', '');
    preg_match('/(\D{1,})*/', '');

    preg_match('/(\D+|\W+)*+/', '');
    preg_match('/(?>(\d+|\w+)*)/', '');