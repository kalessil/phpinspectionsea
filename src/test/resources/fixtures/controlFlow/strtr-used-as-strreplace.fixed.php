<?php

    str_replace(' ', '_', '...');
    str_replace(" ", "_", '...');

    str_replace('\\', '_', '...');
    str_replace("\\", '_', '...');

    str_replace('\'', '_', '...');
    str_replace("\n", '_', '...');

    /* false-positives */
    strtr('...', '...', 'int');
    strtr('...', '\n', 'int');
    strtr('...', "\'", 'int');
    strtr('...', []);