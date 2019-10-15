<?php

    $x = <weak_warning descr="[EA] 'stripos($y, $y)' should be used instead.">strpos(strtolower($y), $y)</weak_warning>;
    $x = <weak_warning descr="[EA] 'strripos($y, $y)' should be used instead.">strrpos($y, strtolower($y))</weak_warning>;

    $x = <weak_warning descr="[EA] 'stripos($y, $y)' should be used instead.">strpos(strtolower($y), strtolower($y))</weak_warning>;
    $x = <weak_warning descr="[EA] 'stripos($y, $y)' should be used instead.">strpos(strtoupper($y), strtoupper($y))</weak_warning>;
    $x = <weak_warning descr="[EA] 'stripos($y, $y)' should be used instead.">strpos(strtolower($y), strtoupper($y))</weak_warning>;