<?php

    <error descr="Please provide second parameter to not influence globals.">parse_str</error> ('');
    <error descr="Please provide second parameter to not influence globals.">mb_parse_str</error> ('');

    /* valid */
    parse_str('', $_GET);
    mb_parse_str('', $_GET);