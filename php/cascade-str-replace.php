<?php

    $str1 = str_replace('-', '_', 'Some text here');
    $str = str_replace('-', '_', 'Some text here');
    // one line comment
    /*
     * multi-line comment
     */
    $str = str_replace(' ', '-', $str);
    $str = str_replace(' ', '-', $str);

    $str2 = str_replace(array('1', '1'), array('1', '1'), $str1);