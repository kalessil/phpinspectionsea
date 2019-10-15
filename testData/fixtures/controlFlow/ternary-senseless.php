<?php

    <warning descr="[EA] Can be replaced with '$y' (reduces cyclomatic complexity and cognitive load).">$x === $y ? $x : $y</warning>;
    <warning descr="[EA] Can be replaced with '$x' (reduces cyclomatic complexity and cognitive load).">$x !== $y ? $x : $y</warning>;

    <warning descr="[EA] Can be replaced with '$x' (reduces cyclomatic complexity and cognitive load).">$x === 0 ? 0 : $x</warning>;
    <warning descr="[EA] Can be replaced with '$x' (reduces cyclomatic complexity and cognitive load).">$x !== 0 ? $x : 0</warning>;
    <warning descr="[EA] Can be replaced with '0' (reduces cyclomatic complexity and cognitive load).">$x === 0 ? $x : 0</warning>;
    <warning descr="[EA] Can be replaced with '0' (reduces cyclomatic complexity and cognitive load).">$x !== 0 ? 0 : $x</warning>;

    <warning descr="[EA] Can be replaced with '$x' (reduces cyclomatic complexity and cognitive load).">$x === '' ? '' : $x</warning>;

    /* false-positives */
    $x == $y ? $x : $y;
    $x ? $x : $y;