<?php

function explode_misuse_count($parameter) {
    /* case: misuse */
    echo substr_count($parameter, '') + 1;
    echo strpos($parameter, '') !== false;
    echo strpos($parameter, '') !== false;

    /* case: misuse, with variants lookup */
    $array = explode('', $parameter);
    echo count($array);

    /* false-positives */
    $result = count(explode('', $parameter, 2));

    $returned = explode('', $parameter);
    $result = count($returned);
    return $returned;
}
