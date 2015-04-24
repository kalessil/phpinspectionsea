<?php
    $obj = new stdClass();
    /* @var string    $time */
    /* @var \DateTime $value */

    foreach (array() as $index => $value) {
        $x = clone $obj;
        $value = (int) $index;

        // warning here
        $objTime = new DateTime($time);

        $strValue = $value->format($time);
        echo $strValue . PHP_EOL;

        /* shall deactivate $objTime = ... */
        //$time = $strValue;
    }

    /**
     * extract variables, exclude ones modified + $this + $value + $key
     * highlight ones left and analyses how to efficiently
     * detect independent statements
     */