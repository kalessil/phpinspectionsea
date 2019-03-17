<?php

function cases_holder() {
    return [
        htmlentities('', ENT_QUOTES | ENT_HTML5),
        htmlentities('', ENT_QUOTES | ENT_HTML5),
        htmlentities('', ENT_QUOTES | ENT_HTML5 | ENT_DISALLOWED),

        htmlentities('', ENT_QUOTES),
        htmlentities('', ENT_COMPAT),
        htmlentities('', ENT_NOQUOTES),
        htmlentities('', ENT_QUOTES | ENT_HTML5),
        htmlentities('', ENT_COMPAT | ENT_HTML5),

        call_user_func('htmlentities', '...'),
        array_map('htmlentities', []),
    ];
}