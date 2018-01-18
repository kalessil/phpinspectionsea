<?php

function cases_holder() {
    return [
        <error descr="Single quotes handling is not specified, please use ENT_QUOTES or ENT_COMPAT as second argument.">htmlentities('')</error>,
        <error descr="Single quotes handling is not specified, please use ENT_QUOTES or ENT_COMPAT as second argument.">htmlentities('', ENT_HTML5)</error>,
        <error descr="Single quotes handling is not specified, please use ENT_QUOTES or ENT_COMPAT as second argument.">htmlentities('', ENT_HTML5 | ENT_DISALLOWED)</error>,

        htmlentities('', ENT_QUOTES),
        htmlentities('', ENT_COMPAT),
        htmlentities('', ENT_QUOTES | ENT_HTML5),
        htmlentities('', ENT_COMPAT | ENT_HTML5),
    ];
}