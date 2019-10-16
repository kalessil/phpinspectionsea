<?php

function cases_holder() {
    return [
        <error descr="[EA] Single quotes handling is not specified, please use ENT_QUOTES or ENT_COMPAT as second argument.">htmlentities('')</error>,
        <error descr="[EA] Single quotes handling is not specified, please use ENT_QUOTES or ENT_COMPAT as second argument.">htmlentities('', ENT_HTML5)</error>,
        <error descr="[EA] Single quotes handling is not specified, please use ENT_QUOTES or ENT_COMPAT as second argument.">htmlentities('', ENT_HTML5 | ENT_DISALLOWED)</error>,

        htmlentities('', ENT_QUOTES),
        htmlentities('', ENT_COMPAT),
        htmlentities('', ENT_NOQUOTES),
        htmlentities('', ENT_QUOTES | ENT_HTML5),
        htmlentities('', ENT_COMPAT | ENT_HTML5),

        call_user_func(<error descr="[EA] Single quotes are not handled, please make use of ENT_QUOTES or ENT_COMPAT flags.">'\htmlentities'</error>, '...'),
        array_map(<error descr="[EA] Single quotes are not handled, please make use of ENT_QUOTES or ENT_COMPAT flags.">'\htmlentities'</error>, []),
    ];
}