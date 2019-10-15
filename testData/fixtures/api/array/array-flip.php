<?php

function cases_holder() {
    return [
        <warning descr="[EA] 'array_flip([])' would fit more here (it also faster).">array_combine([], array_keys([]))</warning>,

        array_combine([], array_keys()),
        array_combine([], array_keys(['...'])),
    ];
}