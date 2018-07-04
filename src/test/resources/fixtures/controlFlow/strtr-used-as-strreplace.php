<?php

    <weak_warning descr="'str_replace(' ', '_', '...')' can be used instead (improves maintainability).">strtr('...', ' ', '_')</weak_warning>;
    <weak_warning descr="'str_replace(\" \", \"_\", '...')' can be used instead (improves maintainability).">strtr('...', " ", "_")</weak_warning>;

    <weak_warning descr="'str_replace('\\', '_', '...')' can be used instead (improves maintainability).">strtr('...', '\\', '_')</weak_warning>;
    <weak_warning descr="'str_replace(\"\\\\\", '_', '...')' can be used instead (improves maintainability).">strtr('...', "\\", '_')</weak_warning>;

    <weak_warning descr="'str_replace('\'', '_', '...')' can be used instead (improves maintainability).">strtr('...', '\'', '_')</weak_warning>;
    <weak_warning descr="'str_replace(\"\n\", '_', '...')' can be used instead (improves maintainability).">strtr('...', "\n", '_')</weak_warning>;

    /* false-positives */
    strtr('...', '...', 'int');
    strtr('...', '\n', 'int');
    strtr('...', "\'", 'int');
    strtr('...', []);