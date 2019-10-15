<?php

    preg_match(<weak_warning descr="[EA] '[0-9]' can be replaced with '\d' (safe in non-unicode mode).">'/[0-9]/'</weak_warning>, '...');
    preg_match(<weak_warning descr="[EA] '[:digit:]' can be replaced with '\d' (safe in non-unicode mode).">'/[:digit:]/'</weak_warning>, '...');
    preg_match(<weak_warning descr="[EA] '[^0-9]' can be replaced with '\D' (safe in non-unicode mode).">'/[^0-9]/'</weak_warning>, '...');
    preg_match(<weak_warning descr="[EA] '[^\d]' can be replaced with '\D' (safe in non-unicode mode).">'/[^\d]/'</weak_warning>, '...');
    preg_match(<weak_warning descr="[EA] '[:word:]' can be replaced with '\w' (safe in non-unicode mode).">'/[:word:]/'</weak_warning>, '...');
    preg_match(<weak_warning descr="[EA] '[A-Za-z0-9_]' can be replaced with '\w' (safe in non-unicode mode).">'/[A-Za-z0-9_]/'</weak_warning>, '...');
    preg_match(<weak_warning descr="[EA] '[^\w]' can be replaced with '\W' (safe in non-unicode mode).">'/[^\w]/'</weak_warning>, '...');
    preg_match(<weak_warning descr="[EA] '[^A-Za-z0-9_]' can be replaced with '\W' (safe in non-unicode mode).">'/[^A-Za-z0-9_]/'</weak_warning>, '...');
    preg_match(<weak_warning descr="[EA] '[^\s]' can be replaced with '\S' (safe in non-unicode mode).">'/[^\s]/'</weak_warning>, '...');