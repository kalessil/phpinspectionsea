Loops:
    'array_merge()/array_fill()/array_combine()': loop + pushing to array
    'array_chunk()':                              loop + array_slice()
    'array_filter()':                             loop + if + pushing to array, loop + if + unset (in some cases even without closure)
    'in_array()/array_search()':                  loop + comparison + break

- LowPerformingFilesystemsOperationsInspection
    - file_exists($file) '&&'|'||' is_file|is_dir|is_link($file): file_exists on left/right is not needed
    - file_exists($file|dir|directory|dirname()|folder|image): is_file|is_dir($...) - faster because of caching
        - is_readable, is_writable also points to file
    - !is_dir( $itemName ) && file_exists( $itemName ) -> is_file


- Amount of traits
- Unused function result, but for generators
- suspicious loops: termination condition variables are not changing while looping
- while ($i < count($array)) { ... } -> SlowArrayOperationsInLoopInspector
- if (not empty check) { foreach() {} } -> if is not needed at all
    - not empty contexts (incl. methods)
    - count/size check (incl. methods)
- SlowArrayOperationsInLoopInspector: counting in while/do-while loops + recognize containers modification
- foreach (file('...') as ...) -> foreach (new SplFileObject('...') as $line)
- explode misuse:
    - explode("\n", file_get_contents('...')) -> file('...', FILE_IGNORE_NEW_LINES)
