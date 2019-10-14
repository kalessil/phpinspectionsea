<?php

function cases_holder($one = null, $two = '', stdClass $three = null) {
    <warning descr="[EA] 'get_class(...)' does not accept null as argument in PHP 7.2+ versions.">get_class(null)</warning>;
    <warning descr="[EA] 'get_class(...)' does not accept null as argument in PHP 7.2+ versions.">get_class($one)</warning>;
    <warning descr="[EA] 'get_class(...)' does not accept null as argument in PHP 7.2+ versions.">get_class($three)</warning>;

    get_class($two);

    if ($one !== null && $three !== null) {
        get_class($one);
        get_class($three);
    }
}