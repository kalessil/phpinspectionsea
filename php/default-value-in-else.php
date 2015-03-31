<?php

    $arr = array();
    if ('' === '') {
        $arr []= '';
    } else {
        $arr []= '';
    }

    $obj = new stdClass();
    if ('' === '') {
        $obj->x = '';
    } else {
        $obj->x = '';
    }
    if ('' === '') {
        $obj::$x = '';
    } else {
        $obj::$x = '';
    }

    $str = '';
    if ('' === '') {
        /* comment */
        $str = '';
        /* comment */
    } elseif ('' === '') {
        $str = '';
        /* comment */
    } else {
        $str = '';
        /* comment */
    }