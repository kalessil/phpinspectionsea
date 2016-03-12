<?php

    class myDateTime extends DateTime {

    }

    $obj = new myDateTime();
    if ($obj instanceof DateTimeInterface || $obj instanceof DateTime || $obj instanceof \myDateTime) {
        echo 123;
    }