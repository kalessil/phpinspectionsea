<?php

    /* following is locale-dependent, leaving to not re-introduce by future people */
    // ctypePatterns.put("^\\d+$",          "ctype_digit");
    // ctypePatterns.put("^[^\\d]+$",       "!ctype_digit");
    // ctypePatterns.put("^[A-Za-z]+$",     "ctype_alpha");
    // ctypePatterns.put("^[^A-Za-z]+$",    "!ctype_alpha");
    // ctypePatterns.put("^[A-Za-z0-9]+$",  "ctype_alnum");
    // ctypePatterns.put("^[^A-Za-z0-9]+$", "!ctype_alnum");

    /* false-positives: locale-dependent ctype_* functions are not to be used at all */
    preg_match('/^\d+$/', $x);
    // preg_match('/^[^\d]+$/', $x);
    preg_match('/^[A-Za-z]+$/', $x);
    preg_match('/^[^A-Za-z]+$/', $x);
    preg_match('/^[A-Za-z0-9]+$/', $x);
    preg_match('/^[^A-Za-z0-9]+$/', $x);
