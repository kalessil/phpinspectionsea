<?php

function cases_holder($one = null, $two = '') {
    <warning descr="'get_class(...)' does not accept null as argument in PHP 7.2+ versions.">get_class(null)<warning>;
    <warning descr="'get_class(...)' does not accept null as argument in PHP 7.2+ versions.">get_class($one)<warning>;

    get_class($two);
}