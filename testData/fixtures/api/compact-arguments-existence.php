<?php

function variable_variables() {
    $value = 'value';
    $name = 'value';

    $reported = 'reported';

    return compact($name, <weak_warning descr="There is chance that it should be 'reported' here.">$reported</weak_warning>);
}