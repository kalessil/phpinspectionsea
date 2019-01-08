<?php

/* fixture checked on 5.6 language level */
return [
    PHP_VERSION_ID === 50300, // reported
    PHP_VERSION_ID === 50600,
    PHP_VERSION_ID === 70000,

    PHP_VERSION_ID == 50300, // reported
    PHP_VERSION_ID == 50600,
    PHP_VERSION_ID == 70000,

    PHP_VERSION_ID !== 50300, // reported
    PHP_VERSION_ID !== 50600,
    PHP_VERSION_ID !== 70000,

    PHP_VERSION_ID != 50300, // reported
    PHP_VERSION_ID != 50600,
    PHP_VERSION_ID != 70000,

    PHP_VERSION_ID > 50300, // reported
    PHP_VERSION_ID > 50600,
    PHP_VERSION_ID > 70000,

    PHP_VERSION_ID >= 50300, // reported
    PHP_VERSION_ID >= 50600, // reported
    PHP_VERSION_ID >= 70000,

    PHP_VERSION_ID < 50300, // reported
    PHP_VERSION_ID < 50600, // reported
    PHP_VERSION_ID < 70000,

    PHP_VERSION_ID <= 50300, // reported
    PHP_VERSION_ID <= 50600,
    PHP_VERSION_ID <= 70000,
];