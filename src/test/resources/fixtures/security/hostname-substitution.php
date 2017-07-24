<?php

    echo '' . '@' . <error descr="...">$_SERVER['SERVER_NAME']</error>;
    echo '@' . <error descr="...">$_SERVER['HTTP_HOST']</error>;
    echo '@' . <error descr="...">strtolower($_SERVER['HTTP_HOST'])</error>;

    $host   = strtolower(<error descr="...">$_SERVER['HTTP_HOST']</error>);
    $domain = strtolower(<error descr="...">$_SERVER['SERVER_NAME']</error>);
    $email  = strtolower(<error descr="...">$_SERVER['HTTP_HOST']</error>);

    function cases_holder() {
        $container = $_SERVER['HTTP_HOST'];
        echo '' . '@' . <error descr="...">$container</error>;
    }
