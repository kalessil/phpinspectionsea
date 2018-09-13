<?php

    preg_match(<error descr="'a-Z' range in '[a-Z]' is looking rather suspicious, please check.">'/[a-Z]/'</error>, '');
    preg_match(<error descr="'a-Z' range in '[^a-Z]' is looking rather suspicious, please check.">'/[^a-Z]/'</error>, '');
    preg_match(<error descr="'A-z' range in '[A-z]' is looking rather suspicious, please check.">'/[A-z]/'</error>, '');
    preg_match(<error descr="'A-z' range in '[^A-z]' is looking rather suspicious, please check.">'/[^A-z]/'</error>, '');

    preg_match(<error descr="'a-Z' range in '[^\[a-Z\]]' is looking rather suspicious, please check.">'/.+[^\[a-Z\]].+/'</error>, '');
    preg_match(<error descr="'9-0' range in '[^\[9-0\]]' is looking rather suspicious, please check.">'/.+[^\[9-0\]].+/'</error>, '');

    preg_match('/\xFF-\x00/', '');
    preg_match('/[\uFFFF-\u0000]/', '');
    preg_match('/[\p{L}-\p{L}]/u', '');
    preg_match('/[^-+]/', '');

    preg_match('/.+a-Z.+/', '');
    preg_match('/.+A-z.+/', '');

    preg_match('/[a-z]/', '');
    preg_match('/[A-Z]/', '');
    preg_match('/[0-8]/', '');
    preg_match('/[1-5]/', '');
    preg_match('/[A-Za-z]/', '');
    preg_match('/[a-zA-Z]/', '');
