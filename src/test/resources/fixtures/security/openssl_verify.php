<?php

function cases_holder($data, $signature, $key) {
    /* unsafe usage: we should compare to 1 for success identification */
    if (<error descr="Please compare with 1 instead (see openssl_verify(...) return codes).">openssl_verify($data, $signature, $key)</error>) {}
    if (<error descr="Please compare with 1 instead (see openssl_verify(...) return codes).">openssl_verify($data, $signature, $key)</error> === 0)  {}
    if (<error descr="Please compare with 1 instead (see openssl_verify(...) return codes).">openssl_verify($data, $signature, $key)</error> !== 0)  {}
    if (<error descr="Please compare with 1 instead (see openssl_verify(...) return codes).">openssl_verify($data, $signature, $key)</error> === -1) {}
    if (<error descr="Please compare with 1 instead (see openssl_verify(...) return codes).">openssl_verify($data, $signature, $key)</error> !== -1) {}

    /* valid usage */
    if (openssl_verify($data, $signature, $key) === 1) {}
    if (openssl_verify($data, $signature, $key) !== 1) {}

    /* unsafe usage: we should compare to 1 for success identification */
    $result = openssl_verify($data, $signature, $key);
    if (<error descr="Please compare with 1 instead (see openssl_verify(...) return codes).">$result</error>) {}

    /* valid usage: we are limited in instrumentation here */
    if ($result === 0)  {}
    if ($result === -1) {}
    if ($result === 1)  {}

    /* unsafe usages: we shoul return bool to avoid flaws */
    return <error descr="Please return '... === 1' instead (to prevent any flaws).">$result</error>;
    return <error descr="Please return '... === 1' instead (to prevent any flaws).">openssl_verify($data, $signature, $key)</error>;
}