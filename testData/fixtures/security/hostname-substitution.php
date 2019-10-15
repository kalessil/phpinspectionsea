<?php

    echo '' . '@' . <error descr="[EA] The email generation can be compromised via '$_SERVER['SERVER_NAME']', consider introducing whitelists.">$_SERVER['SERVER_NAME']</error>;
    echo '@' . <error descr="[EA] The email generation can be compromised via '$_SERVER['HTTP_HOST']', consider introducing whitelists.">$_SERVER['HTTP_HOST']</error>;
    echo '@' . <error descr="[EA] The email generation can be compromised via '$_SERVER['HTTP_HOST']', consider introducing whitelists.">strtolower($_SERVER['HTTP_HOST'])</error>;

    /* false-positives: there are no email-alike criterion matched */
    echo $_SERVER['HTTP_HOST'];
    echo 'HTTP_HOST: ' . $_SERVER['HTTP_HOST'];
    echo 'SERVER_NAME: ' . $_SERVER['SERVER_NAME'];

    $host            = strtolower(<error descr="[EA] The domain here can be compromised, consider introducing whitelists.">$_SERVER['HTTP_HOST']</error>);
    $domain          = strtolower(<error descr="[EA] The domain here can be compromised, consider introducing whitelists.">$_SERVER['SERVER_NAME']</error>);
    $email           = strtolower(<error descr="[EA] The domain here can be compromised, consider introducing whitelists.">$_SERVER['HTTP_HOST']</error>);
    $array['domain'] = strtolower(<error descr="[EA] The domain here can be compromised, consider introducing whitelists.">$_SERVER['HTTP_HOST']</error>);

    function cases_holder_second() {
        if (isset($_SERVER['HTTP_HOST'])) {
            $domain = $_SERVER['HTTP_HOST'];
        }
        echo '' . '@' . <error descr="[EA] The email generation can be compromised via '$_SERVER['HTTP_HOST']', consider introducing whitelists.">$domain</error>;

        if (isset($_SERVER['SERVER_NAME'])) {
            $host = $_SERVER['SERVER_NAME'];
        }
        echo '' . '@' . <error descr="[EA] The email generation can be compromised via '$_SERVER['SERVER_NAME']', consider introducing whitelists.">$host</error>;
    }

    function valid_cases() {
        $server = $_SERVER['SERVER_NAME'] ?? gethostname();
        $host   = $_SERVER['HTTP_HOST']   ?? gethostname();
        /* TODO: this needs to be reported as no in_array presented */
        $domain = $server === $host /*&& in_array($server, [], true)*/ ? $server : 'localhost';
        return $domain;
    }

    function reported_case_simplified() {
        $server = strtolower($_SERVER['SERVER_NAME']);
        if (strpos($server,'www.') === 0) {
            $server = substr($server, 4);
        }
        return 'me@' . <error descr="[EA] The email generation can be compromised via '$_SERVER['SERVER_NAME']', consider introducing whitelists.">$server</error>;
    }

    function whitelisted_case() {
        if (in_array($_SERVER['HTTP_HOST'], [])) {
            $this->server = $_SERVER['HTTP_HOST'];
        }
    }

    function packed_into_array() {
        return [
            'domain' => <error descr="[EA] The domain here can be compromised, consider introducing whitelists.">$_SERVER['HTTP_HOST']</error>,
            $_SERVER['HTTP_HOST'],
        ];
    }
