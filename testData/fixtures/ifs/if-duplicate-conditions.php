<?php

    if ($a || !$a || $a === true || true === $a || $a === null || null === $a) {
        if ($a && !$a && $a === true && true === $a && $a === null && null === $a) {}
    }

    if (empty($a)) {
        if ($a || <warning descr="[EA] This condition is duplicated in another if/elseif branch (replacing duplicates with a local variable would make sense).">empty($a)</warning>) {}
    }

    if (empty($a)) {}
    elseif (<warning descr="[EA] This condition is duplicated in another if/elseif branch (replacing duplicates with a local variable would make sense).">empty($a)</warning>) {}

    if (count($a) > 0 || <warning descr="[EA] This call is duplicated in conditions set.">count($a)</warning> < 0) {}

    /* false-positives: mkdir race conditions */
    if (!is_dir($a) && !@mkdir($a) && !is_dir($a)) {}
    if (is_dir($a) || @mkdir($a) || is_dir($a)) {}