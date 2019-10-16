<?php

    /* logical operands and multi-value cases */
    if ($x == 5 && <error descr="[EA] '$x == 5 && $x == 6' seems to be always false.">$x == 6</error>) {}
    if ($x == 5 && <error descr="[EA] '$x == 5 && $x === 6' seems to be always false.">$x === 6</error>) {}
    if ($x === 5 && <error descr="[EA] '$x === 5 && $x === 6' seems to be always false.">$x === 6</error>) {}
    if ($x != 5 || <error descr="[EA] '$x != 5 || $x != 6' seems to be always true.">$x != 6</error>) {}
    if ($x != 5 || <error descr="[EA] '$x != 5 || $x !== 6' seems to be always true.">$x !== 6</error>) {}
    if ($x !== 5 || <error descr="[EA] '$x !== 5 || $x !== 6' seems to be always true.">$x !== 6</error>) {}
    if (<error descr="[EA] '$x == 5' seems to have no effect due to '$x != 6'.">$x == 5</error> || $x != 6) {}
    if ($x == 5 && <error descr="[EA] '$x != 6' seems to have no effect due to '$x == 5'.">$x != 6</error>) {}

    /* logical operands and multi-value edge-cases */
    if ($x == 5 && <error descr="[EA] '$x == 5' seems to have no effect due to '$x == 5'.">$x == 5</error>) {}
    if ($x != 5 || <error descr="[EA] '$x != 5' seems to have no effect due to '$x != 5'.">$x != 5</error>) {}
    if ($x == 5 || <error descr="[EA] '$x == 5 || $x != 5' seems to be always true.">$x != 5</error>) {}
    if ($x == 5 && <error descr="[EA] '$x == 5 && $x != 5' seems to be always false.">$x != 5</error>) {}

    if ($x == $y && <error descr="[EA] '$y == $x' seems to have no effect due to '$x == $y'.">$y == $x</error>) {}
    if ($x != $y || <error descr="[EA] '$y != $x' seems to have no effect due to '$x != $y'.">$y != $x</error>) {}
    if ($x == $y || <error descr="[EA] '$x == $y || $y != $x' seems to be always true.">$y != $x</error>) {}
    if ($x == $y && <error descr="[EA] '$x == $y && $y != $x' seems to be always false.">$y != $x</error>) {}

    /* false-positives: non-constant values */
    if ($x == 5 && $x == $y) {}
    if ($x == $z && $x == $y) {}

    /* logical operands and multi-value cases for if-statements */
    if ($x === 5) { return <error descr="[EA] '6 == $x' seems to be always false.">6 == $x</error>; }
    if ($x === 5) { return <error descr="[EA] '6 === $x' seems to be always false.">6 === $x</error>; }
    if ($x === 5) { return <error descr="[EA] '6 != $x' seems to be always true.">6 != $x</error>; }
    if ($x === 5) { return <error descr="[EA] '6 !== $x' seems to be always true.">6 !== $x</error>; }

    /* logical operands and multi-value cases for if-statements - edge-cases */
    if ($x === 5 && $whatever) { return <error descr="[EA] '5 === $x' seems to be always true.">5 === $x</error>; }
    if ($x === 5 && $whatever) { return <error descr="[EA] '5 !== $x' seems to be always false.">5 !== $x</error>; }
    if ($x === $y) { return <error descr="[EA] '$y === $x' seems to be always true.">$y === $x</error>; }
    if ($x === $y) { return <error descr="[EA] '$y !== $x' seems to be always false.">$y !== $x</error>; }
    if ($x !== $y) { return <error descr="[EA] '$y !== $x' seems to be always true.">$y !== $x</error>; }
    if ($x !== $y) { return <error descr="[EA] '$y === $x' seems to be always false.">$y === $x</error>; }

    /* false-positives: assignments, non-constant values */
    if ($x === 5) { return $x == $y; }
    if ($x == $z) { return $x == $y; }
    if ($x !== 5) { return $x === 6; }
    if ($x !== 5) { return $x !== 6; }
    if ($x === 5) { $x = '...'; return $x === 6; }