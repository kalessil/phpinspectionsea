<?php

    /* Main case: characters replacement */
    <weak_warning descr="This construct behaves as str_replace(' ', '_', ...), consider refactoring (improves maintainability).">strtr</weak_warning> ('string to fix', ' ', '_');
    <weak_warning descr="This construct behaves as str_replace(\" \", \"_\", ...), consider refactoring (improves maintainability).">strtr</weak_warning> ('string to fix', " ", "_");

    /* False-positives */
    strtr('string to fix', 'string', 'int');
    strtr('string to fix', array('to' => 'to be', 'to be' => 'to'));