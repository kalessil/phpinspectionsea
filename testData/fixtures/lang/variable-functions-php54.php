<?php

    <weak_warning descr="'$callable(1, 2, 3)' would make more sense here (it also faster).">call_user_func ($callable, 1, 2, 3)</weak_warning>;
    <weak_warning descr="'$callable($a, &$b, &$c)' would make more sense here (it also faster).">call_user_func ($callable, $a, &$b, & $c)</weak_warning>;
    <weak_warning descr="'$callable(1, 2, 3)' would make more sense here (it also faster).">forward_static_call ($callable, 1, 2, 3)</weak_warning>;
    <weak_warning descr="'$callable($a, &$b, &$c)' would make more sense here (it also faster).">forward_static_call ($callable, $a, &$b, & $c)</weak_warning>;

    <weak_warning descr="'call_user_func($callable, 1, 2, 3)' would make possible to perform better code analysis here.">call_user_func_array ($callable, array(1, 2, 3))</weak_warning>;
    <weak_warning descr="'call_user_func($callable, 1, 2, 3)' would make possible to perform better code analysis here.">call_user_func_array ($callable, [1, 2, 3])</weak_warning>;
    <weak_warning descr="'call_user_func($callable, 1, 2, 3)' would make possible to perform better code analysis here.">call_user_func_array ($callable, [0 => 1, 'index' => 2, 3])</weak_warning>;

    <weak_warning descr="'forward_static_call($callable, 1, 2, 3)' would make possible to perform better code analysis here.">forward_static_call_array ($callable, array(1, 2, 3))</weak_warning>;
    <weak_warning descr="'forward_static_call($callable, 1, 2, 3)' would make possible to perform better code analysis here.">forward_static_call_array ($callable, [1, 2, 3])</weak_warning>;
    <weak_warning descr="'forward_static_call($callable, 1, 2, 3)' would make possible to perform better code analysis here.">forward_static_call_array ($callable, [0 => 1, 'index' => 2, 3])</weak_warning>;

    <weak_warning descr="'Namespace\Clazz::method($a)' would make more sense here (it also faster).">call_user_func('Namespace\\Clazz::method', $a)</weak_warning>;
    <weak_warning descr="'Namespace\Clazz::method($a)' would make more sense here (it also faster).">forward_static_call('Namespace\\Clazz::method', $a)</weak_warning>;

    /* QF correctness cases */
    <weak_warning descr="'$callable(...$arguments)' would make more sense here (it also faster).">call_user_func($callable, ...$arguments)</weak_warning>;