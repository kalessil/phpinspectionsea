<?php

    preg_match('//', '...');
    preg_match('##', '...');
    preg_match('{}', '...');
    preg_match('<>', '...');
    preg_match('()', '...');
    preg_match('[]', '...');

    preg_match(<warning descr="[EA] The regular expression delimiters are missing (it should be e.g. '/<regex-here>/').">'}{'</warning>, '...');
    preg_match(<warning descr="[EA] The regular expression delimiters are missing (it should be e.g. '/<regex-here>/').">'><'</warning>, '...');
    preg_match(<warning descr="[EA] The regular expression delimiters are missing (it should be e.g. '/<regex-here>/').">')('</warning>, '...');
    preg_match(<warning descr="[EA] The regular expression delimiters are missing (it should be e.g. '/<regex-here>/').">']['</warning>, '...');