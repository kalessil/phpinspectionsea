<?php

function explode_misuse_count($parameter) {
    /* case: misuse */
    echo <warning descr="Consider using 'substr_count($parameter, '') + 1' instead (consumes less cpu and memory resources).">count(explode('', $parameter))</warning>;

    /* case: misuse, with variants lookup */
    $array = explode('', $parameter);
    echo <warning descr="Consider using 'substr_count($parameter, '') + 1' instead (consumes less cpu and memory resources).">count($array)</warning>;

    /* false-positives */
    $result = count(explode('', $parameter, 2));

    $returned = explode('', $parameter);
    $result = count($returned);
    return $returned;
}

function explode_misuse_in_array($parameter) {
    /* case: misuse */
    $result = in_array('...', explode(',', $parameter));

    /* case: misuse, with variants lookup */
    $array = explode(',', $parameter);
    $result = in_array('...', $array);

    /* false-positives */
    $result = in_array('...', explode(',', $parameter, 2));

    $returned = explode('', $parameter);
    echo implode('', $returned);
    return $returned;
}
