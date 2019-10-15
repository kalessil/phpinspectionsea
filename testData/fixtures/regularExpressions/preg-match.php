<?php

function cases_holder() {
    return [
        <warning descr="[EA] '\"text\" !== '...'' can be used instead.">!preg_match('/^text$/', '...')</warning>,
        <warning descr="[EA] '\"text\" !== '...'' can be used instead.">preg_match('/^text$/', '...') < 1</warning>,
        <warning descr="[EA] '\"text\" !== '...'' can be used instead.">preg_match('/^text$/', '...') == 0</warning>,
        <warning descr="[EA] '\"text\" !== '...'' can be used instead.">preg_match('/^text$/', '...') === 0</warning>,
        <warning descr="[EA] '\"text\" !== '...'' can be used instead.">preg_match('/^text$/', '...') != 1</warning>,
        <warning descr="[EA] '\"text\" !== '...'' can be used instead.">preg_match('/^text$/', '...') !== 1</warning>,

        <warning descr="[EA] '0 !== strpos('...', \"text\")' can be used instead.">!preg_match('/^text/', '...')</warning>,
        <warning descr="[EA] '0 !== strpos('...', \"text\")' can be used instead.">preg_match('/^text/', '...') < 1</warning>,
        <warning descr="[EA] '0 !== strpos('...', \"text\")' can be used instead.">preg_match('/^text/', '...') == 0</warning>,
        <warning descr="[EA] '0 !== strpos('...', \"text\")' can be used instead.">preg_match('/^text/', '...') === 0</warning>,
        <warning descr="[EA] '0 !== strpos('...', \"text\")' can be used instead.">preg_match('/^text/', '...') != 1</warning>,
        <warning descr="[EA] '0 !== strpos('...', \"text\")' can be used instead.">preg_match('/^text/', '...') !== 1</warning>,

        <warning descr="[EA] 'false === strpos('...', \"text\")' can be used instead.">!preg_match('/text/', '...')</warning>,
        <warning descr="[EA] 'false === strpos('...', \"text\")' can be used instead.">preg_match('/text/', '...') < 1</warning>,
        <warning descr="[EA] 'false === strpos('...', \"text\")' can be used instead.">preg_match('/text/', '...') == 0</warning>,
        <warning descr="[EA] 'false === strpos('...', \"text\")' can be used instead.">preg_match('/text/', '...') === 0</warning>,
        <warning descr="[EA] 'false === strpos('...', \"text\")' can be used instead.">preg_match('/text/', '...') != 1</warning>,
        <warning descr="[EA] 'false === strpos('...', \"text\")' can be used instead.">preg_match('/text/', '...') !== 1</warning>,
    ];
}