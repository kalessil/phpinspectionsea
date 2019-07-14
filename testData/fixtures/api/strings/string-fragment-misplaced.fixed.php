<?php

function cases_holder($where) {
    return [
        strpos($where, 'fragment'),

        strpos("...$where...", $where),
        strpos($where, 'fragment'),
        strpos('where', 'fragment'),
    ];
}