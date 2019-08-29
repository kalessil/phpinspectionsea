<?php

function cases_holder() {
    return [
        htmlspecialchars('', ENT_QUOTES | ENT_HTML5),
        htmlspecialchars('', ENT_QUOTES | ENT_HTML5),
        htmlspecialchars('', ENT_QUOTES | ENT_HTML5 | ENT_DISALLOWED),

        htmlspecialchars('', ENT_QUOTES),
        htmlspecialchars('', ENT_COMPAT),
        htmlspecialchars('', ENT_NOQUOTES),
        htmlspecialchars('', ENT_QUOTES | ENT_HTML5),
        htmlspecialchars('', ENT_COMPAT | ENT_HTML5),

        call_user_func('\htmlspecialchars', '...'),
        array_map('\htmlspecialchars', []),
    ];
}