<?php

    <weak_warning descr="[EA] 'str_replace(' ', '_', '...')' would fit more here (clarifies intention, improves maintainability).">strtr('...', ' ', '_')</weak_warning>;
    <weak_warning descr="[EA] 'str_replace(\" \", \"_\", '...')' would fit more here (clarifies intention, improves maintainability).">strtr('...', " ", "_")</weak_warning>;

    <weak_warning descr="[EA] 'str_replace('\\', '_', '...')' would fit more here (clarifies intention, improves maintainability).">strtr('...', '\\', '_')</weak_warning>;
    <weak_warning descr="[EA] 'str_replace(\"\\\\\", '_', '...')' would fit more here (clarifies intention, improves maintainability).">strtr('...', "\\", '_')</weak_warning>;

    <weak_warning descr="[EA] 'str_replace('\'', '_', '...')' would fit more here (clarifies intention, improves maintainability).">strtr('...', '\'', '_')</weak_warning>;
    <weak_warning descr="[EA] 'str_replace(\"\n\", '_', '...')' would fit more here (clarifies intention, improves maintainability).">strtr('...', "\n", '_')</weak_warning>;

    /* false-positives */
    strtr('...', '...', 'int');
    strtr('...', '\n', 'int');
    strtr('...', "\'", 'int');
    strtr('...', []);