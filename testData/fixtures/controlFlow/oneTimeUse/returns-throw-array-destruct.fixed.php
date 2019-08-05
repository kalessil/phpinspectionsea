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

function array_assembly()
{
    $filters = ['is_email_compatible' => 1];
    return ['widget_filters' => $filters];
}

function assignment()
{
    $result = '...';
    return trim($result);
}

function method_call_case($argument)
{
    if ($argument) {
        return (new \stdClass())->method()->method();
    } else {
        $two = new \stdClass();
        return $two->method()->method($two);
    }
}

function quick_fixing($argument, $alternative) {
    return ($argument ?? $alternative)->method();

    return ($argument ?: $alternative)->method();
}