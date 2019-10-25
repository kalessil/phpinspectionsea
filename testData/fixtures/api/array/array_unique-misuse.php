<?php

function cases_holder() {
    return [
        <warning descr="[EA] 'array_unique(array_filter(...))' would fit more here (it also slightly faster).">array_filter(array_unique([]))</warning>,
        <warning descr="[EA] 'array_unique(array_filter(...))' would fit more here (it also slightly faster).">array_filter(\array_unique([]))</warning>,
        <warning descr="[EA] 'array_unique(array_filter(...))' would fit more here (it also slightly faster).">\array_filter(array_unique([]))</warning>,

        array_filter(array_unique([]), '....'),
    ];
}