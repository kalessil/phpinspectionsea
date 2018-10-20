<?php

function cases_holder() {
    return [
        <warning descr="'\"text\" !== '...'' can be used instead.">!preg_match('/^text$/', '...')</warning>,
        <warning descr="'\"text\" !== '...'' can be used instead.">preg_match('/^text$/', '...') < 1</warning>,
        <warning descr="'\"text\" !== '...'' can be used instead.">preg_match('/^text$/', '...') == 0</warning>,
        <warning descr="'\"text\" !== '...'' can be used instead.">preg_match('/^text$/', '...') === 0</warning>,
        <warning descr="'\"text\" !== '...'' can be used instead.">preg_match('/^text$/', '...') != 1</warning>,
        <warning descr="'\"text\" !== '...'' can be used instead.">preg_match('/^text$/', '...') !== 1</warning>,

        <warning descr="'0 !== strpos('...', \"text\")' can be used instead.">!preg_match('/^text/', '...')</warning>,
        <warning descr="'0 !== strpos('...', \"text\")' can be used instead.">preg_match('/^text/', '...') < 1</warning>,
        <warning descr="'0 !== strpos('...', \"text\")' can be used instead.">preg_match('/^text/', '...') == 0</warning>,
        <warning descr="'0 !== strpos('...', \"text\")' can be used instead.">preg_match('/^text/', '...') === 0</warning>,
        <warning descr="'0 !== strpos('...', \"text\")' can be used instead.">preg_match('/^text/', '...') != 1</warning>,
        <warning descr="'0 !== strpos('...', \"text\")' can be used instead.">preg_match('/^text/', '...') !== 1</warning>,

        <warning descr="'false === strpos('...', \"text\")' can be used instead.">!preg_match('/text/', '...')</warning>,
        <warning descr="'false === strpos('...', \"text\")' can be used instead.">preg_match('/text/', '...') < 1</warning>,
        <warning descr="'false === strpos('...', \"text\")' can be used instead.">preg_match('/text/', '...') == 0</warning>,
        <warning descr="'false === strpos('...', \"text\")' can be used instead.">preg_match('/text/', '...') === 0</warning>,
        <warning descr="'false === strpos('...', \"text\")' can be used instead.">preg_match('/text/', '...') != 1</warning>,
        <warning descr="'false === strpos('...', \"text\")' can be used instead.">preg_match('/text/', '...') !== 1</warning>,
    ];
}