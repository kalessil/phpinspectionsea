<?php

    preg_match(<weak_warning descr="'[0-9]' can be replaced with '\d' (safe in non-unicode mode).">'/[0-9]/'</weak_warning>, '...');
    preg_match(<weak_warning descr="'[:digit:]' can be replaced with '\d' (safe in non-unicode mode).">'/[:digit:]/'</weak_warning>, '...');
    preg_match(<weak_warning descr="'[^0-9]' can be replaced with '\D' (safe in non-unicode mode).">'/[^0-9]/'</weak_warning>, '...');
    preg_match(<weak_warning descr="'[^\d]' can be replaced with '\D' (safe in non-unicode mode).">'/[^\d]/'</weak_warning>, '...');
    preg_match(<weak_warning descr="'[:word:]' can be replaced with '\w' (safe in non-unicode mode).">'/[:word:]/'</weak_warning>, '...');
    preg_match(<weak_warning descr="'[A-Za-z0-9_]' can be replaced with '\w' (safe in non-unicode mode).">'/[A-Za-z0-9_]/'</weak_warning>, '...');
    preg_match(<weak_warning descr="'[^\w]' can be replaced with '\W' (safe in non-unicode mode).">'/[^\w]/'</weak_warning>, '...');
    preg_match(<weak_warning descr="'[^A-Za-z0-9_]' can be replaced with '\W' (safe in non-unicode mode).">'/[^A-Za-z0-9_]/'</weak_warning>, '...');
    preg_match(<weak_warning descr="'[^\s]' can be replaced with '\S' (safe in non-unicode mode).">'/[^\s]/'</weak_warning>, '...');