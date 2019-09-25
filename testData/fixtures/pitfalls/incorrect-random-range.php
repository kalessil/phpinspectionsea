<?php

    <error descr="The range is not defined properly.">rand(2, 1)</error>;
    rand(1, 2);

    <error descr="The range is not defined properly.">mt_rand(2, 1)</error>;
    mt_rand(1, 2);

    <error descr="The range is not defined properly.">random_int(2, 1)</error>;
    random_int(1, 2);

    <error descr="The range is not defined properly.">random_int(PHP_INT_MAX, PHP_INT_MIN)</error>;
    random_int(PHP_INT_MIN, PHP_INT_MAX);