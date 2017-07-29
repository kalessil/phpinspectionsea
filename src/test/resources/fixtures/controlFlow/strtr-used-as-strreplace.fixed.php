<?php

    /* Main case: characters replacement */
    str_replace(' ', '_', 'string to fix');
    str_replace(" ", "_", 'string to fix');
    str_replace('\\', '_', 'string to fix');

    /* False-positives */
    strtr('string to fix', 'string', 'int');
    strtr('string to fix', array('to' => 'to be', 'to be' => 'to'));