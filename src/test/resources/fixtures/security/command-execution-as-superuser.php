<?php

    $patterns = [
        <error descr="Applications must not require super user privileges, please find another solution.">'sudo ...'</error>,
        <error descr="Applications must not require super user privileges, please find another solution.">"su ..."</error>,
        
        <error descr="Applications must not require super user privileges, please find another solution.">`sudo ...`</error>,
        <error descr="Applications must not require super user privileges, please find another solution.">`su ...`</error>,
        
        'sudo',
        'su',

        `sudo`,
        `su`,
    ];