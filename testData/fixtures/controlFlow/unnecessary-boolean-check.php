<?php

function cases_holder(bool $boolean, string $string) {
    return [
        <weak_warning descr="[EA] '$boolean' would fit better here (reduces cognitive load).">!$boolean !== true</weak_warning>,
        <weak_warning descr="[EA] '$boolean' would fit better here (reduces cognitive load).">$boolean === true</weak_warning>,
        <weak_warning descr="[EA] '!$boolean' would fit better here (reduces cognitive load).">!$boolean === true</weak_warning>,
        <weak_warning descr="[EA] '!$boolean' would fit better here (reduces cognitive load).">$boolean !== true</weak_warning>,
        <weak_warning descr="[EA] '!$boolean' would fit better here (reduces cognitive load).">!$boolean !== false</weak_warning>,
        <weak_warning descr="[EA] '!$boolean' would fit better here (reduces cognitive load).">$boolean === false</weak_warning>,
        <weak_warning descr="[EA] '$boolean' would fit better here (reduces cognitive load).">!$boolean === false</weak_warning>,
        <weak_warning descr="[EA] '$boolean' would fit better here (reduces cognitive load).">$boolean !== false</weak_warning>,

        /* false-positives: non-booleans, weak operators */
        $string === true,
        $boolean == true,
        $boolean != false,
    ];
}