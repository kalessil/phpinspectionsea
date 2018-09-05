<?php

function cases_holder()
{
    echo <warning descr="'__DIR__' should be used instead.">dirname(__FILE__)</warning>;

    echo dirname(__FILE__.'/..');
    echo dirname();
}