<?php

    preg_match(<error descr="(...+...)* can be exploited (ReDoS, Regular Expression Denial of Service)">'/(?:\D+|0(?!1))*/'</error>, '');
    preg_match(<error descr="(...+...)+ can be exploited (ReDoS, Regular Expression Denial of Service)">'/(?:\D+|0(?!1))+/'</error>, '');
    preg_match(<error descr="(...+...){10} can be exploited (ReDoS, Regular Expression Denial of Service)">'/(?:\D+|0(?!1)){10}/'</error>, '');
    preg_match(<error descr="(...+...){1,} can be exploited (ReDoS, Regular Expression Denial of Service)">'/(?:\D+|0(?!1)){1,}/'</error>, '');
    preg_match(<error descr="(...+...){1,10} can be exploited (ReDoS, Regular Expression Denial of Service)">'/(?:\D+|0(?!1)){1,10}/'</error>, '');

    preg_match('/(?:\D+|0(?!1))++/', '');
    preg_match('/((?:\D+|0(?!1))+)/', '');