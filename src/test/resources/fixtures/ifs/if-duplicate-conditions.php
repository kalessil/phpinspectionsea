<?php

    if ($a || !$a || $a === true || true === $a) {
        if ($a || !$a || $a === true || true === $a) {
            echo $a;
        }
    }

    if (empty($a)) {
        if($a || <warning descr="This condition is duplicated in another if/elseif branch.">empty($a)</warning>) {
            echo $a;
        }
    }
