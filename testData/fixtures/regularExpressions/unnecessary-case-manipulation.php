<?php

    $x = preg_match('/x{1,3}/', <warning descr="Unnecessary case manipulation (use i-flag in regex for better performance).">strtolower('...')</warning>);
    $x = preg_match('/x{1,3}/', <warning descr="Unnecessary case manipulation (use i-flag in regex for better performance).">strtoupper('...')</warning>);
    $x = preg_match('/x{1,3}/', <warning descr="Unnecessary case manipulation (use i-flag in regex for better performance).">mb_strtoupper('...')</warning>);
    $x = preg_match('/x{1,3}/', <warning descr="Unnecessary case manipulation (use i-flag in regex for better performance).">mb_strtolower('...')</warning>);

    $x = preg_match('/x{1,3}/i', <warning descr="Unnecessary case manipulation (the regex is case-insensitive).">strtolower('...')</warning>);
    $x = preg_match('/x{1,3}/i', <warning descr="Unnecessary case manipulation (the regex is case-insensitive).">strtoupper('...')</warning>);
    $x = preg_match('/x{1,3}/i', <warning descr="Unnecessary case manipulation (the regex is case-insensitive).">mb_strtoupper('...')</warning>);
    $x = preg_match('/x{1,3}/i', <warning descr="Unnecessary case manipulation (the regex is case-insensitive).">mb_strtolower('...')</warning>);

    $x = preg_match('/x{1,3}/',strtolower('...'), $m);