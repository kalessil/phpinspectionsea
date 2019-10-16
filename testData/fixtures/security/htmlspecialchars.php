<?php

function cases_holder() {
    return [
        <error descr="[EA] Single quotes handling is not specified, please use ENT_QUOTES or ENT_COMPAT as second argument.">htmlspecialchars('')</error>,
        <error descr="[EA] Single quotes handling is not specified, please use ENT_QUOTES or ENT_COMPAT as second argument.">htmlspecialchars('', ENT_HTML5)</error>,
        <error descr="[EA] Single quotes handling is not specified, please use ENT_QUOTES or ENT_COMPAT as second argument.">htmlspecialchars('', ENT_HTML5 | ENT_DISALLOWED)</error>,

        htmlspecialchars('', ENT_QUOTES),
        htmlspecialchars('', ENT_COMPAT),
        htmlspecialchars('', ENT_NOQUOTES),
        htmlspecialchars('', ENT_QUOTES | ENT_HTML5),
        htmlspecialchars('', ENT_COMPAT | ENT_HTML5),

        call_user_func(<error descr="[EA] Single quotes are not handled, please make use of ENT_QUOTES or ENT_COMPAT flags.">'\htmlspecialchars'</error>, '...'),
        array_map(<error descr="[EA] Single quotes are not handled, please make use of ENT_QUOTES or ENT_COMPAT flags.">'\htmlspecialchars'</error>, []),
    ];
}