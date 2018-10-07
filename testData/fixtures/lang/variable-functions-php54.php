<?php

    <weak_warning descr="'$callable(1, 2, 3)' should be used instead.">call_user_func ($callable, 1, 2, 3)</weak_warning>;
    <weak_warning descr="'$callable($a, &$b, &$c)' should be used instead.">call_user_func ($callable, $a, &$b, & $c)</weak_warning>;

    <weak_warning descr="'call_user_func($callable, 1, 2, 3)' should be used instead (enables further analysis).">call_user_func_array ($callable, array(1, 2, 3))</weak_warning>;
    <weak_warning descr="'call_user_func($callable, 1, 2, 3)' should be used instead (enables further analysis).">call_user_func_array ($callable, [1, 2, 3])</weak_warning>;
    <weak_warning descr="'call_user_func($callable, 1, 2, 3)' should be used instead (enables further analysis).">call_user_func_array ($callable, [0 => 1, 'index' => 2, 3])</weak_warning>;

    <weak_warning descr="'Namespace\Clazz::method($a)' should be used instead.">call_user_func('Namespace\\Clazz::method', $a)</weak_warning>;