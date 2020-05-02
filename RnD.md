Loops:
    'array_merge()/array_fill()/array_combine()': loop + pushing to array
    'array_chunk()':                              loop + array_slice()
    'array_filter()':                             loop + if + pushing to array, loop + if + unset (in some cases even without closure)
    'in_array()/array_search()':                  loop + comparison + break

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
    
- evaluate:
    - (directoryContext || fileContext) && (directoryContext != fileContext) getting reported

