Loops:
    'array_merge()/array_fill()': loop + pushing to array
    'array_chunk()':              loop + array_slice()
    'array_column()':             loop + index access
    'array_sum()':                loop + summing up
    'array_product()':            loop + multiplication
    'implode()':                  loop + concatenation
    'array_filter()':             loop + if + pushing to array
    'array_map()':                loop + transforming value
    'in_array()/array_search()':  loop + comparison + break

// evaluate if parse_url() misused and can be narrowed down to
parse_url(..., CONSTANT-HERE)
pathinfo(..., CONSTANT-HERE)

//filter_var()
FILTER_VALIDATE_IP should probably come with FILTER_FLAG_NO_PRIV_RANGE, FILTER_FLAG_NO_RES_RANGE


array_key_exists(0, $array) && $array[0] !== null -> isset($array[0])

https://rules.sonarsource.com/php/RSPEC-4423: insecure SSL versions
    TLS 1, 1.1, 1.2 is insecure (FREAK, POODLE, BEAST, CRIME; https://www.acunetix.com/blog/articles/tls-ssl-cipher-hardening/)
    SSL 2, 3        is insecure (POODLE; https://www.acunetix.com/blog/articles/tls-ssl-cipher-hardening/)
    
    + STREAM_CRYPTO_METHOD_TLS_CLIENT, STREAM_CRYPTO_METHOD_TLSv1_0_CLIENT, STREAM_CRYPTO_METHOD_TLSv1_1_CLIENT
    -> STREAM_CRYPTO_METHOD_TLSv1_2_CLIENT
    
    ? STREAM_CRYPTO_PROTO_TLSv1_0, STREAM_CRYPTO_PROTO_TLSv1_1
    -> STREAM_CRYPTO_PROTO_TLSv1_2
    
    ? STREAM_CRYPTO_METHOD_SSLv2_CLIENT, STREAM_CRYPTO_METHOD_SSLv23_CLIENT, STREAM_CRYPTO_METHOD_SSLv3_CLIENT
    ? STREAM_CRYPTO_PROTO_SSLv3
    
    ? CURL_SSLVERSION_MAX_*
    
Infinity loop: the arguments are not modified, the call can be nested somewhere

Suspicious binary expressions: (!x &&) || x

array_filter([], function ($value) { return $value != falsy-value; }); // unnecessary closure
array_filter([], function ($value) { return $value === null; })
array_reduce($array, 'array_merge', $init) // array_merge misused -> array_merge($init, ...$array)
array_unique(array_filter([]) // needs to be swapped -> array_filter(array_unique([])