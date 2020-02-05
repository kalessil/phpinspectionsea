Loops:
    'array_merge()/array_fill()/array_combine()': loop + pushing to array
    'array_chunk()':                              loop + array_slice()
    'array_column()':                             loop + index access
    'array_filter()':                             loop + if + pushing to array, loop + if + unset (in some cases even without closure)
    'in_array()/array_search()':                  loop + comparison + break

Amount of traits
Unused function result, but for generators

A typo-safe programming language is a language were the Levenstein distance between any two names in the same scope must be 2 or more.
 -> variables, properties and methods names