<?php

    echo preg_quote('');
    echo '#'.preg_quote('');
    echo '/'.preg_quote('').'/';
    echo "/".preg_quote('')."/";

    echo preg_quote('', '/');

    /* false -positives */
    echo preg_quote('', '');
    echo '#'.preg_quote('', '');
    echo '/'.preg_quote('', '').'/';
