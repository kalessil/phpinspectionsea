<?php

    preg_match('/[seq].../', '');
    preg_match(<weak_warning descr="'[seq][seq]' can be replaced with '[seq]{...}'.">'/[seq][seq].../'</weak_warning>, '');
    preg_match(<weak_warning descr="'[seq][seq]+' can be replaced with '[seq]{...}'.">'/[seq][seq]+/'</weak_warning>, '');
    preg_match(<weak_warning descr="'[seq][seq]*' can be replaced with '[seq]{...}'.">'/[seq][seq]*/'</weak_warning>, '');
    preg_match(<weak_warning descr="'[seq][seq]?' can be replaced with '[seq]{...}'.">'/[seq][seq]?/'</weak_warning>, '');
    preg_match(<weak_warning descr="'[seq]+[seq]*[seq]?[seq]{1,}' can be replaced with '[seq]{...}'.">'/[seq]+[seq]*[seq]?[seq]{1,}/'</weak_warning>, '');