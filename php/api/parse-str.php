<?php

    /* reported */
    parse_str('');
    mb_parse_str('');

    /* valid */
    parse_str('', $_GET);
    mb_parse_str('', $_GET);