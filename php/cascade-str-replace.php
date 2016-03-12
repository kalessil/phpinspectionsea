<?php

    $str1 = str_replace('-', '_', 'Some text here');
    $str = str_replace('-', '_', 'Some text here');
    // one line comment
    /*
     * multi-line comment
     */
    $str = str_replace(' ', '-', $str); // <- reported
    $str = str_replace(' ', '-', $str); // <- reported

    /* nested calls */
    $str = str_replace(' ', '-', str_replace(' ', '-', $str)); // <- reported inner call

    $str2 = str_replace(array('1', '1'), array('1', '1'), $str1); // <- reported 2nd argument