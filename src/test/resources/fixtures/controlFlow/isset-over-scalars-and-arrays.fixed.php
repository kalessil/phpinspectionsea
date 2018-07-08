<?php

$array = [];
if (isset($array['0' . 1])) {
    /* multiple isset, we do nothing to not conflict with other inspections */
    $a = isset($array['index'], $array['index']);

    /* generally used constructs we'll not report */
    $b = isset($array['index']) ? $array['index'] : null;
    $b = !isset($array['index']) ? null : $array['index'];

    echo isset($array[$index]);
    echo !isset($array[$index]);

    /* if isset value used, we only suggesting to use array_key_exists */
    $tmp []= isset($array['property']);
    $tmp []= !isset($array['property']);
    $x = isset($array['0' . 1]);
    $x = !isset($array['0' . 1]);
    return isset($array['0' . 1]);
}

/* case: variables in global/function/method scopes */
function variablesScopeHolder(array $array)
{
    try {
        $x = $array !== null;
        $y = $array === null;
    } finally {
        $x = isset($array);
        $y = !isset($array);
    }
}
$y = !isset($array);
