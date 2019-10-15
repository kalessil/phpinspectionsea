<?php

function cases_holder($where) {
    return [
        strpos(<error descr="[EA] ''fragment'' should probably be the second argument (replace with a constant if intended).">'fragment'</error>, $where),

        strpos("...$where...", $where),
        strpos($where, 'fragment'),
        strpos('where', 'fragment'),
    ];
}