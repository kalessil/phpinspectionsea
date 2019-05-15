<?php

function cases_holder($x) {
    return [
        <warning descr="Inlining nested 'array_merge(...)' in arguments is possible here (it also faster).">array_merge([], array_merge($x, []), [])</warning>,
        <warning descr="Inlining nested 'array_merge(...)' in arguments is possible here (it also faster).">array_merge(array_merge($x, []))</warning>,

        <warning descr="'array_push(...)' would fit more here (it also faster).">$x = array_merge([0])</warning>,
        <warning descr="'array_push(...)' would fit more here (it also faster).">$x = array_merge($x, [0])</warning>,
        <warning descr="'array_push(...)' would fit more here (it also faster).">$x = array_merge($x, [0, 1, 2])</warning>,
        $x = array_merge($x, []),
        $x = array_merge($x, ['key' => 'value']),

        $x = <warning descr="'[...]' would fit more here (it also much faster).">array_merge([], [])</warning>,
        $x = <warning descr="'[...]' would fit more here (it also much faster).">array_merge(['...'], ['...' => '...'])</warning>,
        $x = <warning descr="'[...]' would fit more here (it also much faster).">array_merge(['...' => '...'], ['...'])</warning>,

        <warning descr="'array_unshift(...)' would fit more here (it also faster).">$x = array_merge([0], $x)</warning>,
        <warning descr="'array_unshift(...)' would fit more here (it also faster).">$x = array_merge([0, 1, 2], $x)</warning>,
        $x = array_merge([], $x),
        $x = array_merge(['key' => 'value'], $x),
    ];
}