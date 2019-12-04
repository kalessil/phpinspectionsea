<?php

    if (!in_array('', array())) {}
    if (in_array('', array())) {}

    if (in_array('', array()) || false) {}
    if (in_array('', array()) or false) {}
    if (in_array('', array()) OR false) {}

    if (in_array('', array()) && true) {}
    if (in_array('', array()) and true) {}
    if (in_array('', array()) AND true) {}

    if (!in_array('', array())) {}
    if (!in_array('', array())) {}

    if (array_search('', array()) !== true) {}
    if (true !== array_search('', array())) {}

    if (in_array('', array())) {}
    if (in_array('', array())) {}

    if (array_search('', array()) === true) {}
    if (true === array_search('', array())) {}

    /* false-positives */
    $x = array_search('', []) ?: '...';
    $x = $x ?: array_search('', []);
    $x = $x ?? array_search('', []);

    function cases_holder()
    {
        $key = array_search('', array());
        if ($key === false) {
            return;
        }

        $index = array_search('', $array = array());
        if ($index !== false) {
            unset($array[$index]);
        }
    }