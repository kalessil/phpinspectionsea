<?php

function cases_holder() {
    return [
        <error descr="Single quotes handling is not specified, please use ENT_QUOTES or ENT_COMPAT as second argument.">htmlentities</error>(''),
        htmlentities('', ENT_QUOTES),
    ];
}