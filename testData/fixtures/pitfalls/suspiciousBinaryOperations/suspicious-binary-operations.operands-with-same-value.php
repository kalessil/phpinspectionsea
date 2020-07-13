<?php

function cases_holder($x, $y, $z) {
    return [
        <error descr="[EA] Same value used in the operation (the operation is incorrect or can be simplified).">$x + $y == $x</error>,
        <error descr="[EA] Same value used in the operation (the operation is incorrect or can be simplified).">$x + $y === $x</error>,
        <error descr="[EA] Same value used in the operation (the operation is incorrect or can be simplified).">$x + $y != $x</error>,
        <error descr="[EA] Same value used in the operation (the operation is incorrect or can be simplified).">$x + $y !== $x</error>,
        <error descr="[EA] Same value used in the operation (the operation is incorrect or can be simplified).">$x + $y > $x</error>,
        <error descr="[EA] Same value used in the operation (the operation is incorrect or can be simplified).">$x + $y >= $x</error>,
        <error descr="[EA] Same value used in the operation (the operation is incorrect or can be simplified).">$x + $y < $x</error>,
        <error descr="[EA] Same value used in the operation (the operation is incorrect or can be simplified).">$x + $y <= $x</error>,

        <error descr="[EA] Same value used in the operation (the operation is incorrect or can be simplified).">$x - $y == $x</error>,
        <error descr="[EA] Same value used in the operation (the operation is incorrect or can be simplified).">$x * $y == $x</error>,
        <error descr="[EA] Same value used in the operation (the operation is incorrect or can be simplified).">$x / $y == $x</error>,

        $x + $y == $z,
        $x % $y == $x,
    ];
}