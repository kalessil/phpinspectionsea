<?php

function explode_misuse_count($parameter) {
    /* case: misuse */
    echo substr_count($parameter, '') + 1;
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

function explode_misuse_in_array($parameter) {
    /* case: misuse */
    $result = strpos($parameter, ',' . '...' . ',') !== false;

    /* case: replacement generation */
    $result = strpos($parameter, ',' . '...' . ',') === false;
    $result = (strpos($parameter, ',' . '...' . ',') !== false) === true;

    /* case: misuse, with variants lookup */
    $array = explode(',', $parameter);
    $result = in_array('...', $array);

    /* false-positives */
    $result = in_array('...', explode(',', $parameter, 2));

    $returned = explode('', $parameter);
    echo implode('', $returned);
    return $returned;
}
