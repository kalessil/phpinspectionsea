Loops:
    'array_merge()/array_fill()/array_combine()/array_values()/array_keys()/$copy = $array': loop + pushing to array
    'array_chunk()':                                                                         loop + array_slice()

- (performance) foreach (file('...') as ...) -> foreach (new SplFileObject('...') as $line)

- Amount of traits
- Unused function result, but for generators
- suspicious loops: termination condition variables are not changing while looping
- if (not empty check) { foreach() {} } -> not needed at all
    - not empty contexts (incl. methods)
    - count/size check (incl. methods)
- marker traits -> marker interfaces: no methods, checked via class_uses(...)
- review suspicious binary operation:  strategies && ExpressionSemanticUtil.getExpressionTroughParenthesis

