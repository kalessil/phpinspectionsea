<?php

    echo true  && $x === null;
    echo false || $x === null;

    echo $x === null;
    echo $x === null;
    echo $x === null;
    echo $x === null;
    echo $x === null;

    echo $x !== null;
    echo $x !== null;
    echo $x !== null;
    echo $x !== null;
    echo $x !== null;

    echo ($x = null) !== null;
    echo ($x ?: null) !== null;
    echo ($x ?? null) !== null;
    echo ($x ? null : null) !== null;

    echo is_null();