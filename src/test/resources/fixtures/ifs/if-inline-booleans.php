<?php

    if (<error descr="This boolean in condition makes no sense or enforces condition result">true</error>)          ;
    if (<error descr="This boolean in condition makes no sense or enforces condition result">false</error>)         ;
    if (!<error descr="This boolean in condition makes no sense or enforces condition result">true</error>)         ;
    if (!<error descr="This boolean in condition makes no sense or enforces condition result">false</error>)        ;
    if (<error descr="This boolean in condition makes no sense or enforces condition result">true</error> || $x)    ;
    if (<error descr="This boolean in condition makes no sense or enforces condition result">true</error> && $x)    ;

    /* false-positives */
    if (null)          ;