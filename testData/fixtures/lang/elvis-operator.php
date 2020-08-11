<?php

    $x = <weak_warning descr="[EA] It's possible to use '$y ?: 0' here (shorter notation).">$y ? $y : 0</weak_warning>;
    $x = <weak_warning descr="[EA] It's possible to use '$y ?: 0' here (shorter notation).">$y ? (($y)) : 0</weak_warning>;
    $x = <weak_warning descr="[EA] It's possible to use '$y ?: 0' here (shorter notation).">($y) ? $y : 0</weak_warning>;
    $x = <weak_warning descr="[EA] It's possible to use '$y ?: 0' here (shorter notation).">((($y))) ? ($y) : 0</weak_warning>;

    /* false-positives */
    $x = $y ? 0 : $y;
    $x = !$y ? $y : 0;