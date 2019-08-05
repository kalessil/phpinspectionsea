<?php

return 1;

return returnByReference()->x;

return (new \stdClass())->x;

return (clone $x)->x;

throw new Exception();

function list_unpack()
{
    list($a, $b) = array(1, 2);
    return $a + $b;
}

function array_assembling()
{
    $filters = ['is_email_compatible' => 1];
    return ['widget_filters' => $filters];
}

function quick_fixing($argument, $alternative) {
    return ($argument ?? $alternative)->property;

    return ($argument ?: $alternative)->property;
}