<?php

function cases_holder() {
    return [
        <warning descr="[EA] 'array_map(..., array_slice(...))' would make more sense here (it also faster).">array_slice(array_map('...', []), -1)</warning>,
        <warning descr="[EA] 'array_map(..., array_slice(...))' would make more sense here (it also faster).">array_slice(array_map('...', []), -1, 1)</warning>,
    ];
}