<?php

    if ($a && count($a) > 0)           {}
    if (is_array($a) && count($a) > 0) {}

    /* basic cases */
    if (
        count($a) > 0 ||
        <weak_warning descr="[EA] This condition execution costs less than the previous one.">isset($b)</weak_warning>
    ) {}
    if (
        $a->count() > 0 ||
        <weak_warning descr="[EA] This condition execution costs less than the previous one.">isset($b)</weak_warning>
    ) {}

    /* more complex cases */
    if (
        isset($x[uniqid()]) &&
        <weak_warning descr="[EA] This condition execution costs less than the previous one.">$x</weak_warning>
    ) {}
