<?php

    <error descr="[EA] This relies on include_path and not guaranteed to load the right file. Concatenate with __DIR__ or use namespaces + class loading instead.">include ('file.php')</error>;
    <error descr="[EA] This relies on include_path and not guaranteed to load the right file. Concatenate with __DIR__ or use namespaces + class loading instead.">include_once ('file.php')</error>;
    <error descr="[EA] This relies on include_path and not guaranteed to load the right file. Concatenate with __DIR__ or use namespaces + class loading instead.">require ('file.php')</error>;
    <error descr="[EA] This relies on include_path and not guaranteed to load the right file. Concatenate with __DIR__ or use namespaces + class loading instead.">require_once ('file.php')</error>;

    <error descr="[EA] This relies on include_path and not guaranteed to load the right file. Concatenate with __DIR__ or use namespaces + class loading instead.">include 'file.php'</error>;
    <error descr="[EA] This relies on include_path and not guaranteed to load the right file. Concatenate with __DIR__ or use namespaces + class loading instead.">include_once 'file.php'</error>;
    <error descr="[EA] This relies on include_path and not guaranteed to load the right file. Concatenate with __DIR__ or use namespaces + class loading instead.">require 'file.php'</error>;
    <error descr="[EA] This relies on include_path and not guaranteed to load the right file. Concatenate with __DIR__ or use namespaces + class loading instead.">require_once 'file.php'</error>;

    function functionWithInclusion () {
        $file = 'file.php';
        <error descr="[EA] This relies on include_path and not guaranteed to load the right file. Concatenate with __DIR__ or use namespaces + class loading instead.">include $file</error>;
    }

    /* false-positives */
    include __DIR__.'file.php';
    include '/file.php';
    include 'c://file.php';