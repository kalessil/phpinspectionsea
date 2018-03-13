<?php

function cases_holder($data, $signature, $key) {
    /* unsafe usage */
    if (openssl_verify($data, $signature, $key)) {}
    if (openssl_verify($data, $signature, $key) === 0)  {}
    if (openssl_verify($data, $signature, $key) !== 0)  {}
    if (openssl_verify($data, $signature, $key) === -1) {}
    if (openssl_verify($data, $signature, $key) !== -1) {}

    /* valid usage */
    if (openssl_verify($data, $signature, $key) === 1) {}
    if (openssl_verify($data, $signature, $key) !== 1) {}

    /* unsafe usage */
    $result = openssl_verify($data, $signature, $key);
    if ($result) {}

    /* valid usage */
    if ($result === 0)  {}
    if ($result === 1)  {}
    if ($result === -1) {}

    /* unsafe usages (better to return `... === 1`) */
    return $result;
    return openssl_verify($data, $signature, $key);
}