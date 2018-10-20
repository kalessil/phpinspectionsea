<?php

function cases_holder() {
    return [
        "text" !== '...',
        "text" !== '...',
        "text" !== '...',
        "text" !== '...',
        "text" !== '...',
        "text" !== '...',

        0 !== strpos('...', "text"),
        0 !== strpos('...', "text"),
        0 !== strpos('...', "text"),
        0 !== strpos('...', "text"),
        0 !== strpos('...', "text"),
        0 !== strpos('...', "text"),

        false === strpos('...', "text"),
        false === strpos('...', "text"),
        false === strpos('...', "text"),
        false === strpos('...', "text"),
        false === strpos('...', "text"),
        false === strpos('...', "text"),
    ];
}