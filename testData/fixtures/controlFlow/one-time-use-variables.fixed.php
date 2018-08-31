<?php

return 1;

return returnByReference()->x;

return (new \stdClass())->x;

return (clone $x)->x;

throw new Exception();

function listUnpack()
{
    list($a, $b) = array(1, 2);
    return $a + $b;
}

function arrayAssembling()
{
    $filters = ['is_email_compatible' => 1];
    return ['widget_filters' => $filters];
}