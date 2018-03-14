<?php

function cases_holder($data, $signature, $key) {
    /* unsafe usage */
    if (<error descr="Please compare with 1 instead (see openssl_verify(...) return codes).">openssl_verify($data, $signature, $key)</error>) {}
    if (openssl_verify($data, $signature, $key) === 0)  {}
    if (openssl_verify($data, $signature, $key) !== 0)  {}
    if (openssl_verify($data, $signature, $key) === -1) {}
    if (openssl_verify($data, $signature, $key) !== -1) {}

    /* valid usage */
    if (openssl_verify($data, $signature, $key) === 1) {}
    if (openssl_verify($data, $signature, $key) !== 1) {}

    /* unsafe usage */
    $result = openssl_verify($data, $signature, $key);
    if (<error descr="Please compare with 1 instead (see openssl_verify(...) return codes).">$result</error>) {}

    /* valid usage */
    if ($result === 0)  {}
    if ($result === 1)  {}
    if ($result === -1) {}

    /* unsafe usages (better to return `... === 1`) */
    return <error descr="Please return '... === 1' instead (to prevent any flaws).">$result</error>;
    return <error descr="Please return '... === 1' instead (to prevent any flaws).">openssl_verify($data, $signature, $key)</error>;
}