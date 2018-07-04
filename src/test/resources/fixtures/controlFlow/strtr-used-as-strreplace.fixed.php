<?php

    str_replace(' ', '_', '...');
    str_replace(" ", "_", '...');
    
    str_replace('\\', '_', '...');
    str_replace("\\", '_', '...');
    
    str_replace('\'', '_', '...');
    str_replace("\"", '_', '...');

    /* false-positives */
    strtr('...', '...', 'int');
    strtr('...', '\n', 'int');
    strtr('...', "\'", 'int');
    strtr('...', []);