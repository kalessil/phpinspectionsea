<?php

    if ($condition) {
    }<error descr="It's probably was intended to use 'else if' or 'elseif' here.">if</error> ($condition) ;

    if ($condition) {
    } <error descr="It's probably was intended to use 'else if' or 'elseif' here.">if</error> ($condition) ;

    if ($condition) {
    } elseif ($condition) {
    } <error descr="It's probably was intended to use 'else if' or 'elseif' here.">if</error> ($condition) ;

    if ($condition) {
    } else if ($condition) {
    } <error descr="It's probably was intended to use 'else if' or 'elseif' here.">if</error> ($condition) ;

    if ($condition) {
    } else {
    } if ($condition) ;