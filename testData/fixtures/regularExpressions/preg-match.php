<?php

function cases_holder() {
    return [
        // "text" !== '...'
        !preg_match('/^text$/', '...'),
        preg_match('/^text$/', '...') < 1,
        preg_match('/^text$/', '...') == 0,
        preg_match('/^text$/', '...') === 0,
        preg_match('/^text$/', '...') != 1,
        preg_match('/^text$/', '...') !== 1,

        // 0 !== strpos('...', "text")
        !preg_match('/^text/', '...'),
        preg_match('/^text/', '...') < 1,
        preg_match('/^text/', '...') == 0,
        preg_match('/^text/', '...') === 0,
        preg_match('/^text/', '...') != 1,
        preg_match('/^text/', '...') !== 1,

        // false === strpos('...', "text")
        !preg_match('/text/', '...'),
        preg_match('/text/', '...') < 1,
        preg_match('/text/', '...') == 0,
        preg_match('/text/', '...') === 0,
        preg_match('/text/', '...') != 1,
        preg_match('/text/', '...') !== 1,
    ];
}