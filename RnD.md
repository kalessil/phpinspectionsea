Loops:
    'array_merge()/array_fill/array_combine()': loop + pushing to array
    'array_chunk()':                            loop + array_slice()
    'array_column()':                           loop + index access
    'array_sum()':                              loop + summing up
    'array_product()':                          loop + multiplication
    'implode()':                                loop + concatenation
    'array_filter()':                           loop + if + pushing to array
    'array_map()':                              loop + transforming value
    'in_array()/array_search()':                loop + comparison + break

// evaluate if parse_url() misused and can be narrowed down to
parse_url(..., CONSTANT-HERE)
pathinfo(..., CONSTANT-HERE)

//filter_var()
FILTER_VALIDATE_IP should probably come with FILTER_FLAG_NO_PRIV_RANGE, FILTER_FLAG_NO_RES_RANGE

https://rules.sonarsource.com/php/RSPEC-4423: insecure SSL versions
    TLS 1, 1.1, 1.2 is insecure (FREAK, POODLE, BEAST, CRIME; https://www.acunetix.com/blog/articles/tls-ssl-cipher-hardening/)
    SSL 2, 3        is insecure (POODLE; https://www.acunetix.com/blog/articles/tls-ssl-cipher-hardening/)
    
    define("STREAM_CRYPTO_METHOD_TLSv1_0_CLIENT", 9);
    define("STREAM_CRYPTO_METHOD_TLSv1_1_CLIENT", 17);
        -> define("STREAM_CRYPTO_METHOD_TLSv1_2_CLIENT", 33);
    
    define("STREAM_CRYPTO_PROTO_TLSv1_0", 8);
    define("STREAM_CRYPTO_PROTO_TLSv1_1", 16);
        -> define("STREAM_CRYPTO_PROTO_TLSv1_2", 32);
    
    define ('STREAM_CRYPTO_METHOD_SSLv2_CLIENT', 0);
    define ('STREAM_CRYPTO_METHOD_SSLv23_CLIENT', 2);
        -> define ('STREAM_CRYPTO_METHOD_SSLv3_CLIENT', 1);
    
    define('CURL_SSLVERSION_MAX_TLSv1_0', 262144);
    define('CURL_SSLVERSION_MAX_TLSv1_1', 327680);
    define('CURL_SSLVERSION_MAX_TLSv1_2', 393216);
        -> define('CURL_SSLVERSION_MAX_TLSv1_3', 458752);