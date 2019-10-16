<?php

    $patterns = [
        <error descr="[EA] Applications must not require super user privileges, please find another solution.">'sudo ...'</error>,
        <error descr="[EA] Applications must not require super user privileges, please find another solution.">"su ..."</error>,
        
        <error descr="[EA] Applications must not require super user privileges, please find another solution.">`sudo ...`</error>,
        <error descr="[EA] Applications must not require super user privileges, please find another solution.">`su ...`</error>,
        
        'sudo',
        'su',

        `sudo`,
        `su`,
    ];