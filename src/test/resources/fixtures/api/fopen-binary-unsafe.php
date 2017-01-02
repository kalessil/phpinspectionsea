<?php

function fopenBUS()
{
    $mode = 'w';
    fopen('', <error descr="The mode is not binary-safe ('b' is missing).">$mode</error>);
    fopen('', <error descr="The mode is not binary-safe (replace 't' with 'b').">'wt'</error>);

    fopen('', 'wtb');
}