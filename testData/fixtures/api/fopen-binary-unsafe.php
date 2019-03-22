<?php

function case_holder()
{
    $mode = 'w';
    fopen('', <warning descr="The mode is not binary-safe ('b' is missing, as documentation recommends).">$mode</warning>);
    fopen('', <warning descr="The mode is not binary-safe (replace 't' with 'b', as documentation recommends).">'wt'</warning>);

    fopen('', 'wtb');
    fopen('', 'b');
    fopen('', '');

    fopen('', <error descr="The 'b' modifier needs to be the last one (e.g 'wb', 'wb+').">'bw+'</error>);
}