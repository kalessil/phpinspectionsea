<?php

    preg_match(<error descr="\d and \w are not mutually exclusive in '\d*\w*' which can be exploited (ReDoS, Regular Expression Denial of Service).">'/\d*\w*/'</error>, '');
    preg_match(<error descr="\d and \w are not mutually exclusive in '\w+\d+' which can be exploited (ReDoS, Regular Expression Denial of Service).">'/\w+\d+/'</error>, '');
    preg_match(<error descr="\D and \W are not mutually exclusive in '\D*\W+' which can be exploited (ReDoS, Regular Expression Denial of Service).">'/\D*\W+/'</error>, '');
    preg_match(<error descr="\D and \W are not mutually exclusive in '\W+\D*' which can be exploited (ReDoS, Regular Expression Denial of Service).">'/\W+\D*/'</error>, '');

    preg_match('/\d\w/', '');
    preg_match('/\w\d/', '');
    preg_match('/\D\W/', '');
    preg_match('/\W\D/', '');

    preg_match('/\w*\s*\d*/', '');

    preg_match('/\d+\W+/', '');
    preg_match('/\W*\d*/', '');
    preg_match('/\D+\w*/', '');
    preg_match('/\w*\D+/', '');
