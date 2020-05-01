Loops:
    'array_merge()/array_fill()/array_combine()': loop + pushing to array
    'array_chunk()':                              loop + array_slice()
    'array_column()':                             loop + index access
    'array_filter()':                             loop + if + pushing to array, loop + if + unset (in some cases even without closure)
    'in_array()/array_search()':                  loop + comparison + break

- Amount of traits
- Unused function result, but for generators
- suspicious loops: termination condition variables are not changing while looping
- while ($i < count($array)) { ... } -> SlowArrayOperationsInLoopInspector
- if (not empty check) { foreach() {} } -> if is not needed at all
    - not empty contexts (incl. methods)
    - count/size check (incl. methods)
- https://github.com/symfony/symfony/issues/36493
- SlowArrayOperationsInLoopInspector: counting in while/do-while loops + recognize containers modification
- LowPerformingDirectoryOperationsInspection -> LowPerformingFilesystemsOperationsInspection
    - file_exists($file) '&&'|'||' is_file|is_dir|is_link($file): file_exists on left/right is not needed
    - file_exists($file|dir|directory|folder): is_file|is_dir($...) - faster because of caching
