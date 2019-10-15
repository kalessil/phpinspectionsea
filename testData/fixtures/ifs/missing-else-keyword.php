<?php

    if ($condition) {
    }<error descr="[EA] It's probably was intended to use 'else if' or 'elseif' here.">if</error> ($condition) ;

    if ($condition) {
    } <error descr="[EA] It's probably was intended to use 'else if' or 'elseif' here.">if</error> ($condition) ;

    if ($condition) {
    } elseif ($condition) {
    } <error descr="[EA] It's probably was intended to use 'else if' or 'elseif' here.">if</error> ($condition) ;

    if ($condition) {
    } else if ($condition) {
    } <error descr="[EA] It's probably was intended to use 'else if' or 'elseif' here.">if</error> ($condition) ;

    if ($condition) {
    } else {
    } if ($condition) ;