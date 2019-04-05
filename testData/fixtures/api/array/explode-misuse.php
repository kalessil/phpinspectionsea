<?php

function explode_misuse_count($parameter) {
    /* case: misuse */
    echo <warning descr="Consider using 'substr_count($parameter, '') + 1' instead (consumes less cpu and memory resources).">count(explode('', $parameter))</warning>;
    echo <warning descr="Consider using 'strpos($parameter, '') !== false' instead (consumes less cpu and memory resources).">count(explode('', $parameter)) > 1</warning>;

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
    $result = <warning descr="Consider using 'strpos($parameter, ','.'...'.',') !== false' instead (consumes less cpu and memory resources).">in_array('...', explode(',', $parameter))</warning>;

    /* case: replacement generation */
    $result = <warning descr="Consider using 'strpos($parameter, ','.'...'.',') === false' instead (consumes less cpu and memory resources).">!in_array('...', explode(',', $parameter))</warning>;
    $result = <warning descr="Consider using '(strpos($parameter, ','.'...'.',') !== false)' instead (consumes less cpu and memory resources).">in_array('...', explode(',', $parameter))</warning> === true;

    /* case: misuse, with variants lookup */
    $array = explode(',', $parameter);
    $result = <warning descr="Consider using 'strpos($parameter, ','.'...'.',') !== false' instead (consumes less cpu and memory resources).">in_array('...', $array)</warning>;

    /* false-positives */
    $result = in_array('...', explode(',', $parameter, 2));

    $returned = explode('', $parameter);
    echo implode('', $returned);
    return $returned;
}
