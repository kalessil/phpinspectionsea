<?php

    preg_match(<error descr="(...+...)* might be exploited (ReDoS, Regular Expression Denial of Service).">'/(?:\D+|0(?!1))*/'</error>, '');
    preg_match(<error descr="(...+...)+ might be exploited (ReDoS, Regular Expression Denial of Service).">'/(?:\D+|0(?!1))+/'</error>, '');
    preg_match(<error descr="(...+...){10} might be exploited (ReDoS, Regular Expression Denial of Service).">'/(?:\D+|0(?!1)){10}/'</error>, '');
    preg_match(<error descr="(...+...){1,} might be exploited (ReDoS, Regular Expression Denial of Service).">'/(?:\D+|0(?!1)){1,}/'</error>, '');
    preg_match(<error descr="(...+...){1,10} might be exploited (ReDoS, Regular Expression Denial of Service).">'/(?:\D+|0(?!1)){1,10}/'</error>, '');

    preg_match('/(\D+|\W+)*+/', '');
    preg_match('/(?>(\d+|\w+)*)/', '');