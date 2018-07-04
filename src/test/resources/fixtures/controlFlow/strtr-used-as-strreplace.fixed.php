<?php

    strtr('...', ' ', '_');
    strtr('...', " ", "_");
    
    strtr('...', '\\', '_');
    strtr('...', "\\", '_');
    
    strtr('...', '\'', '_');
    strtr('...', "\"", '_');

    /* false-positives */
    strtr('...', '...', 'int');
    strtr('...', '\n', 'int');
    strtr('...', "\'", 'int');
    strtr('...', []);