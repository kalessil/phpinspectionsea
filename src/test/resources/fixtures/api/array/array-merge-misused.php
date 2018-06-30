<?php

function cases_holder($x) {
    return [
        <warning descr="Inlining nested 'array_merge(...)' in arguments is possible here (it also faster).">array_merge([], array_merge([], []), [])</warning>,

        <warning descr="'array_push(...)' would fit more here (it also faster).">$x = array_merge($x, [0])</warning>,
        <warning descr="'array_push(...)' would fit more here (it also faster).">$x = array_merge($x, [0, 1, 2])</warning>,
        $x = array_merge($x, []),
        $x = array_merge($x, ['key' => 'value']),
    ];
}