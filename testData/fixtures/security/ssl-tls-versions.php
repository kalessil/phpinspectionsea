<?php

function cases_holder() {
    return [
        <warning descr="This SSL version is weak, please consider using newer one.">STREAM_CRYPTO_METHOD_TLSv1_0_CLIENT</warning>,
        <warning descr="This SSL version is weak, please consider using newer one.">STREAM_CRYPTO_PROTO_TLSv1_0</warning>,
        <warning descr="This SSL version is weak, please consider using newer one.">CURL_SSLVERSION_MAX_TLSv1_0</warning>,
        <warning descr="This TLS version is weak, please consider using newer one.">STREAM_CRYPTO_METHOD_SSLv2_CLIENT</warning>,

        STREAM_CRYPTO_METHOD_TLSv1_2_CLIENT,
        STREAM_CRYPTO_PROTO_TLSv1_2,
        CURL_SSLVERSION_MAX_TLSv1_3,
        STREAM_CRYPTO_METHOD_SSLv3_CLIENT,
    ];
}