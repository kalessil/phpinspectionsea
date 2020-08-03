<?php

    $callable(1, 2, 3);
    $callable($a, &$b, &$c);
    $callable(1, 2, 3);
    $callable($a, &$b, &$c);

    call_user_func($callable, 1, 2, 3);
    call_user_func($callable, 1, 2, 3);
    call_user_func($callable, 1, 2, 3);

    forward_static_call($callable, 1, 2, 3);
    forward_static_call($callable, 1, 2, 3);
    forward_static_call($callable, 1, 2, 3);

    Namespazz\Clazz::method($a);
    Namespazz\Clazz::method($a);

    /* QF correctness cases */
    $callable(...$arguments);