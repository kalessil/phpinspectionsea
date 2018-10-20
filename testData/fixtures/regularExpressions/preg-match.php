<?php

function cases_holder() {
    return [
        !preg_match('^text$', '...'),
        preg_match('^text$', '...') < 1,
        preg_match('^text$', '...') == 0,
        preg_match('^text$', '...') === 0,
        preg_match('^text$', '...') != 1,
        preg_match('^text$', '...') !== 1,

        !preg_match('^text', '...'),
        preg_match('^text', '...') < 1,
        preg_match('^text', '...') == 0,
        preg_match('^text', '...') === 0,
        preg_match('^text', '...') != 1,
        preg_match('^text', '...') !== 1,

        !preg_match('text', '...'),
        preg_match('text', '...') < 1,
        preg_match('text', '...') == 0,
        preg_match('text', '...') === 0,
        preg_match('text', '...') != 1,
        preg_match('text', '...') !== 1,
    ];
}