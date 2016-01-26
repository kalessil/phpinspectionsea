<?php

    echo strtr('string to fix', ' ', '_'); // <- to be reported

    echo strtr('string to fix', 'string', 'int'); // <- do not report
    echo strtr('string to fix', array('to' => 'to be', 'to be' => 'to')); // <- do not report