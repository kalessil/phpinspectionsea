<?php

function nullable(Clazz $parameter): ?Clazz {}
function not_null(Clazz $parameter): Clazz  {}

function cases_holder() {
    $var = nullable(new Clazz());
    $var = nullable(<warning descr="Null pointer exception may occur here.">$var</warning>);
    $var = not_null(<warning descr="Null pointer exception may occur here.">$var</warning>);
    $var = not_null($var);
}