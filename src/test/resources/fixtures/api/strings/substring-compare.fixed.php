<?php

function cases_holder() {
    return [
        strtolower(substr('', 0, 0)) == '',
        strtolower(substr('', 0, 0)) != '',
        strtolower(substr('', 0, 0)) === '',
        strtolower(substr('', 0, 0)) !== '',

        substr('', 0, 0) == '',
        substr('', 0, 0) === '',

        substr('', 0) == '',
        substr('', 0) === '',
        substr('', -1) === 'a',
        substr('', 1) === 'a',

        substr('...', 0, -3) == '...',
        substr('...', 0, 3) === '...',
    ];
}
