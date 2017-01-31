<?php

function fopenBUS()
{
    $mode = 'w';
    fopen('', <warning descr="The mode is not binary-safe ('b' is missing).">$mode</warning>);
    fopen('', <warning descr="The mode is not binary-safe (replace 't' with 'b').">'wt'</warning>);

    fopen('', 'wtb');
}