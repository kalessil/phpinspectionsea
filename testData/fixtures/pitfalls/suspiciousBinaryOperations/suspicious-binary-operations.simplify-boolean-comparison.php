<?php

function cases_holder($x, $y, $z, $t) {
    if (($x && $y) || (<error descr="[EA] '($x && $y) || (!$x && !$y)' is the same as '(bool) $x === (bool) $y', please review the conditions.">!$x && !$y</error>)) {}
    if (($x && !$y) || (<error descr="[EA] '($x && !$y) || (!$x && $y)' is the same as '(bool) $x !== (bool) $y', please review the conditions.">!$x && $y</error>)) {}
    if (($x && !$y) || ($z && !$t) || (<error descr="[EA] '($x && !$y) || (!$x && $y)' is the same as '(bool) $x !== (bool) $y', please review the conditions.">!$x && $y</error>)) {}

    if (($x === true && $y) || (<error descr="[EA] '($x === true && $y) || ($x === false && !$y)' is the same as '(bool) $x === (bool) $y', please review the conditions.">$x === false && !$y</error>)) {}
    if (($x !== false && $y) || (<error descr="[EA] '($x !== false && $y) || ($x !== true && !$y)' is the same as '(bool) $x === (bool) $y', please review the conditions.">$x !== true && !$y</error>)) {}

    if (($x || $y) && (<error descr="[EA] '($x || $y) && ($x != $y)' is the same as '(bool) $x !== (bool) $y', please review the conditions.">$x != $y</error>)) {}
    if (($x || $y) && (<error descr="[EA] '($x || $y) && ($x !== $y)' is the same as '(bool) $x !== (bool) $y', please review the conditions.">$x !== $y</error>)) {}
    if (($x === true || $y) && (<error descr="[EA] '($x === true || $y) && ($x !== $y)' is the same as '(bool) $x !== (bool) $y', please review the conditions.">$x !== $y</error>)) {}
    if (($x || $y === true) && (<error descr="[EA] '($x || $y === true) && ($x !== $y)' is the same as '(bool) $x !== (bool) $y', please review the conditions.">$x !== $y</error>)) {}

    /* false-positives: empty()-handling */
    if ((!empty($x) && $y) || ($x && !$y)) {}
    if (($x && $y) || (empty($x) && !$y)) {}
}