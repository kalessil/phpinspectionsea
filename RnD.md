Loops:
    'array_merge()/array_fill()/array_combine()': loop + pushing to array
    'array_chunk()':                              loop + array_slice()
    'array_column()':                             loop + index access
    'array_filter()':                             loop + if + pushing to array, loop + if + unset (in some cases even without closure)
    'in_array()/array_search()':                  loop + comparison + break

Amount of traits
Unused function result, but for generators

- is_iterable, is_countable, str_contains, str_starts_with, str_ends_with
    - recognize polyfills (global scope)
    - Ultimate