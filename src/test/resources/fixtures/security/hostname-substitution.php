<?php

    echo '' . '@' . <error descr="...">$_SERVER['SERVER_NAME']</error>;
    echo '@' . <error descr="...">$_SERVER['HTTP_HOST']</error>;
    echo '@' . <error descr="...">strtolower($_SERVER['HTTP_HOST'])</error>;

    /* false-positives: there are no email-alike criterion matched */
    echo $_SERVER['HTTP_HOST'];
    echo 'HTTP_HOST: ' . $_SERVER['HTTP_HOST'];
    echo 'SERVER_NAME: ' . $_SERVER['SERVER_NAME'];

    $host   = strtolower(<error descr="...">$_SERVER['HTTP_HOST']</error>);
    $domain = strtolower(<error descr="...">$_SERVER['SERVER_NAME']</error>);
    $email  = strtolower(<error descr="...">$_SERVER['HTTP_HOST']</error>);

    function cases_holder_second() {
        if (isset($_SERVER['HTTP_HOST'])) {
            $domain = $_SERVER['HTTP_HOST'];
        } else {
            $domain = $_SERVER['SERVER_NAME'];
        }
        echo '' . '@' . <error descr="...">$domain</error>;
    }

    function valid_cases() {
        $server = $_SERVER['SERVER_NAME'] ?? gethostname();
        $host   = $_SERVER['HTTP_HOST']   ?? gethostname();
        /* TODO: this needs to be reported as no in_array presented */
        $domain = $server === $host /*&& in_array($server, [], true)*/ ? $server : 'localhost';
        return $domain;
    }

    function reported_case_simplified(){
        $server = strtolower($_SERVER['SERVER_NAME']);
        if (strpos($server,'www.') === 0) {
            $server = substr($server, 4);
        }
        return 'me@' . <error descr="...">$server</error>;
    }
