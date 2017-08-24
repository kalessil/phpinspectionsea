<?php

    class ClassNeedsToStringMethod {}

    /* pattern: object can not be used in string context */
    $object = new ClassNeedsToStringMethod();
    $result = $object == '...';
    $result = $object != '...';
    $result = $object <> '...';

    /* pattern: safe comparison */
    $result = $x === '...';
    $result = $x !== '...';
    $result = $x !== '...';

    /* pattern: needs hardening */
    $result = $x == '';
    $result = $x != '';
    $result = $x == '0';
    $result = $x != '0';
    $result = $x == $y;
    $result = $x != $y;