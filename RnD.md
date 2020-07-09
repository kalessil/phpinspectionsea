Loops:
    'array_filter()':                             loop + if + pushing to array, loop + if + unset (in some cases even without closure)
    'array_merge()/array_fill()/array_combine()': loop + pushing to array
    'array_chunk()':                              loop + array_slice()
    'in_array()/array_search()':                  loop + comparison + break

- (performance) foreach (file('...') as ...) -> foreach (new SplFileObject('...') as $line)

- Amount of traits
- Unused function result, but for generators
- suspicious loops: termination condition variables are not changing while looping
- if (not empty check) { foreach() {} } -> not needed at all
    - not empty contexts (incl. methods)
    - count/size check (incl. methods)
- marker traits -> marker interfaces: no methods, checked via class_uses(...)

