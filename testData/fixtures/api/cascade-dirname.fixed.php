<?php

    dirname(__DIR__, 3);
    dirname(trim(__DIR__), 3);
    dirname(__DIR__, 1 + $level);
    dirname(__DIR__, 3);

    /* false-positives */
    dirname(realpath(__DIR__), 2);
    dirname();
    dirname(dirname());
    dirname(__DIR__);
    dirname(__DIR__, 1);
    dirname(trim(__DIR__));
    dirname(trim(__DIR__), 1);
    dirname(__DIR__, 3);