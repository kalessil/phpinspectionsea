<?php

    /* false-positives: a constant initialization */
    class Clazz {
        const SINGLE_ARRAY_CONSTANT = [];
        const ADDED_ARRAY_CONSTANT  = self::SINGLE_ARRAY_CONSTANT + self::SINGLE_ARRAY_CONSTANT;
    }