<?php

function case_holder()
{
    $mode = 'w';
    fopen('', <warning descr="The mode is not binary-safe ('b' is missing).">$mode</warning>);
    fopen('', <warning descr="The mode is not binary-safe (replace 't' with 'b').">'wt'</warning>);

    fopen('', 'wtb');
    fopen('', 'b');
    fopen('', '');

    fopen('', <error descr="The 'b' modifier needs to be the last one (e.g 'wb', 'wb+').">'bw+'</error>);
}