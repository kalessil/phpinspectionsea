<?php

    $parameter = '';
    $one = function ($parameter) use (<error descr="There is a parameter named 'parameter' already.">$parameter</error>) {
    };
    $two = function ($parameter) {
        static <error descr="There is a parameter named 'parameter' already.">$parameter</error> = null;
    };
    $three = function () use ($parameter) {
        static <error descr="There is a use-argument named 'parameter' already.">$parameter</error> = null;
    };