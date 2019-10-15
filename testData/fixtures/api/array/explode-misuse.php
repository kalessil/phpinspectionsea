<?php

function explode_misuse_count($parameter) {
    /* case: misuse */
    echo <warning descr="[EA] Consider using 'substr_count($parameter, '') + 1' instead (consumes less cpu and memory resources).">count(explode('', $parameter))</warning>;
    echo <warning descr="[EA] Consider using 'strpos($parameter, '') !== false' instead (consumes less cpu and memory resources).">count(explode('', $parameter)) > 1</warning>;
    echo <warning descr="[EA] Consider using 'strpos($parameter, '') !== false' instead (consumes less cpu and memory resources).">count(explode('', $parameter)) >= 2</warning>;

    /* case: misuse, with variants lookup */
    $array = explode('', $parameter);
    echo <warning descr="[EA] Consider using 'substr_count($parameter, '') + 1' instead (consumes less cpu and memory resources).">count($array)</warning>;

    /* false-positives */
    $result = count(explode('', $parameter, 2));

    $returned = explode('', $parameter);
    $result = count($returned);
    return $returned;
}
