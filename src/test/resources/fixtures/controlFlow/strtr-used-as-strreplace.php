<?php

    /* Main case: characters replacement */
    <weak_warning descr="'str_replace(' ', '_', 'string to fix')' can be used instead (improves maintainability).">strtr('string to fix', ' ', '_')</weak_warning>;
    <weak_warning descr="'str_replace(\" \", \"_\", 'string to fix')' can be used instead (improves maintainability).">strtr('string to fix', " ", "_")</weak_warning>;
    <weak_warning descr="'str_replace('\\', '_', 'string to fix')' can be used instead (improves maintainability).">strtr('string to fix', '\\', '_')</weak_warning>;

    /* False-positives */
    strtr('string to fix', 'string', 'int');
    strtr('string to fix', array('to' => 'to be', 'to be' => 'to'));