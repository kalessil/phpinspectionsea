<?php

    if (<weak_warning descr="'isset($a) && $a' can be replaced with '!empty($a)'.">isset($a)</weak_warning> && $a) {}
    if ($a && <weak_warning descr="'isset($a) && $a' can be replaced with '!empty($a)'.">isset($a)</weak_warning>) {}
    if (isset(<weak_warning descr="'isset($a) && $a' can be replaced with '!empty($a)'.">$a</weak_warning>, $b) && $a) {}
    if (isset(<weak_warning descr="'isset($a) && $a' can be replaced with '!empty($a)'.">$a</weak_warning>, $b) && $a && $c) {}

    if (isset($a) || $a) {}
    if (isset($a) || $b) {}