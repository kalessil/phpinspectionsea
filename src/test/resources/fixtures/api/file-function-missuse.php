<?php

    $content = <error descr="'file_get_contents('...')' would consume less cpu and memory resources here.">join('', file('...'))</error>;

    $content = <error descr="'file_get_contents('...')' would consume less cpu and memory resources here.">implode('', file('...'))</error>;
    $content = @<error descr="'file_get_contents('...')' would consume less cpu and memory resources here.">implode('', file('...'))</error>;
    $content = <error descr="'file_get_contents('...')' would consume less cpu and memory resources here.">implode('', @file('...'))</error>;

    $a = '';
    $content = <error descr="'file_get_contents('...')' would consume less cpu and memory resources here.">implode($a, file('...'))</error>;

    $content = implode('-', file('...'));
    $content = implode('', file('...', FILE_IGNORE_NEW_LINES));
    $content = implode('', file('...', FILE_USE_INCLUDE_PATH, null));