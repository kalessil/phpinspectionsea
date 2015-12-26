<?php

    $x = in_array('1', array(), true);
    $x = in_array('1', array('2'), true);
    $x = in_array('1', array(0 => '2'), true);

    $y = in_array('0', array_keys(array('first item')), false);