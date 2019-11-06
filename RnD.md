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
