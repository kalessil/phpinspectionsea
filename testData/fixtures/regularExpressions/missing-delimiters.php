<?php

    preg_match(<warning descr="The regular expression delimiters are missing (it should be e.g. '/<regex-here>/').">'fragment.+'</warning>, '...');
    preg_match('/fragment.+/', '...');

    preg_match(
        '/
        multi
        line
        /',
        $str
    );