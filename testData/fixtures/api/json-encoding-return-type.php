<?php

function cases_holder($encoded) {
    return [
        <weak_warning descr="[EA] Please specify the second argument (clarifies decoding into array or object).">json_decode($encoded)</weak_warning>,
        json_decode($encoded, true),
        json_decode($encoded, false),
    ];
}