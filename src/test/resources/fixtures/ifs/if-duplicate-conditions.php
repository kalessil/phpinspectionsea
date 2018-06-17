<?php

    if ($a || !$a || $a === true || true === $a || $a === null || null === $a) {
        if ($a && !$a && $a === true && true === $a && $a === null && null === $a) {}
    }

    if (empty($a)) {
        if ($a || <warning descr="This condition is duplicated in another if/elseif branch.">empty($a)</warning>) {}
    }

    if (empty($a)) {}
    elseif (<warning descr="This condition is duplicated in another if/elseif branch.">empty($a)</warning>) {}

    if (count($a) > 0 || <warning descr="This call is duplicated in conditions set.">count($a)</warning> < 0) {}

    /* false-positives: mkdir race conditions */
    if (!is_dir($a) && !@mkdir($a) && !is_dir($a)) {}
    if (is_dir($a) || @mkdir($a) || is_dir($a)) {}