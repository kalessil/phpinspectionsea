<?php

function cases_holder() {
    return [
        <error descr="Single quotes handling is not specified, please use ENT_QUOTES or ENT_COMPAT as second argument.">htmlspecialchars('')</error>,
        <error descr="Single quotes handling is not specified, please use ENT_QUOTES or ENT_COMPAT as second argument.">htmlspecialchars('', ENT_HTML5)</error>,
        <error descr="Single quotes handling is not specified, please use ENT_QUOTES or ENT_COMPAT as second argument.">htmlspecialchars('', ENT_HTML5 | ENT_DISALLOWED)</error>,

        htmlspecialchars('', ENT_QUOTES),
        htmlspecialchars('', ENT_COMPAT),
        htmlspecialchars('', ENT_QUOTES | ENT_HTML5),
        htmlspecialchars('', ENT_COMPAT | ENT_HTML5),
    ];
}