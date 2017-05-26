<?php

    if ($a || !$a || $a === true || true === $a) {
        if ($a && !$a && $a === true && true === $a) {}
    }

    if (empty($a)) {
        if ($a || <warning descr="This condition is duplicated in another if/elseif branch.">empty($a)</warning>) {}
    }

    if (empty($a)) {}
    elseif (<warning descr="This condition is duplicated in another if/elseif branch.">empty($a)</warning>) {}

    if (count($a) > 0 || <warning descr="This call is duplicated in conditions set.">count($a)</warning> < 0) {}
