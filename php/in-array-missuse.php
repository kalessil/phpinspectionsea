<?php

    $x = in_array('1', array(), true);
    $x = in_array('1', array('1'), true);

    $y = in_array('0', array_keys(array('first item')), false);