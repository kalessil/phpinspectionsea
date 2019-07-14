<?php

function cases_holder($where) {
    return [
        strpos(<error descr="''fragment'' should be the second argument.">'fragment'</error>, $where),

        strpos("...$where...", $where),
        strpos($where, 'fragment'),
        strpos('where', 'fragment'),
    ];
}