Loops:
    'array_merge()/array_fill()/array_combine()': loop + pushing to array
    'array_chunk()':                              loop + array_slice()
    'array_column()':                             loop + index access
    'array_sum()':                                loop + summing up
    'array_product()':                            loop + multiplication
    'array_filter()':                             loop + if + pushing to array
    'array_map()':                                loop + transforming value
    'array_flip()':                               loop + swapping key-value pairs
    'in_array()/array_search()':                  loop + comparison + break
    'implode()':                                  loop + concatenation

parse_url/pathinfo(...) + accessing only single element of the result:
    -> suggest using a constant as second argument

    parse_url:
        * scheme    -> PHP_URL_SCHEME
        * host      -> PHP_URL_HOST
        * port      -> PHP_URL_PORT
        * user      -> PHP_URL_USER
        * pass      -> PHP_URL_PASS
        * path      -> PHP_URL_PATH
        * query     -> PHP_URL_QUERY
        * fragment  -> PHP_URL_FRAGMENT    
    pathinfo
        * dirname   -> PATHINFO_DIRNAME
        * basename  -> PATHINFO_BASENAME
        * extension -> PATHINFO_EXTENSION
        * filename  -> PATHINFO_FILENAME