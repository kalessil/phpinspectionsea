<?php
    $x = gettype($x) === 'unknown type';
    $x = gettype($x) === 'resource (closed)';

    /* incorrect type checked */
    $x = gettype($x) === <error descr="'oops' is not a value returned by 'gettype(...)'.">'oops'</error>;
    /* aliasing taken into account */
    $x = <warning descr="'is_float($x)' would fit more here (clearer expresses the intention and SCA friendly).">gettype($x) === 'double'</warning>;
    $x = <warning descr="'is_int($x)' would fit more here (clearer expresses the intention and SCA friendly).">gettype($x) === 'integer'</warning>;

    /* all functions handled correctly */
    $x = <warning descr="'is_bool($x)' would fit more here (clearer expresses the intention and SCA friendly).">gettype($x) === 'boolean'</warning>;
    $x = <warning descr="'is_int($x)' would fit more here (clearer expresses the intention and SCA friendly).">gettype($x) === 'integer'</warning>;
    $x = <warning descr="'is_float($x)' would fit more here (clearer expresses the intention and SCA friendly).">gettype($x) === 'double'</warning>;
    $x = <warning descr="'is_string($x)' would fit more here (clearer expresses the intention and SCA friendly).">gettype($x) === 'string'</warning>;
    $x = <warning descr="'is_array($x)' would fit more here (clearer expresses the intention and SCA friendly).">gettype($x) === 'array'</warning>;
    $x = <warning descr="'is_object($x)' would fit more here (clearer expresses the intention and SCA friendly).">gettype($x) === 'object'</warning>;
    $x = <warning descr="'is_resource($x)' would fit more here (clearer expresses the intention and SCA friendly).">gettype($x) === 'resource'</warning>;
    $x = <warning descr="'is_null($x)' would fit more here (clearer expresses the intention and SCA friendly).">gettype($x) === 'NULL'</warning>;

    /* comparison operands are handled correctly */
    $x = <warning descr="'is_string($x)' would fit more here (clearer expresses the intention and SCA friendly).">gettype($x) === 'string'</warning>;
    $x = <warning descr="'!is_string($x)' would fit more here (clearer expresses the intention and SCA friendly).">gettype($x) !== 'string'</warning>;
    $x = <warning descr="'is_string($x)' would fit more here (clearer expresses the intention and SCA friendly).">'string' === gettype($x)</warning>;
    $x = <warning descr="'!is_string($x)' would fit more here (clearer expresses the intention and SCA friendly).">'string' !== gettype($x)</warning>;
    $x = <warning descr="'is_string($x)' would fit more here (clearer expresses the intention and SCA friendly).">gettype($x) == 'string'</warning>;
    $x = <warning descr="'!is_string($x)' would fit more here (clearer expresses the intention and SCA friendly).">gettype($x) != 'string'</warning>;
    $x = <warning descr="'is_string($x)' would fit more here (clearer expresses the intention and SCA friendly).">'string' == gettype($x)</warning>;
    $x = <warning descr="'!is_string($x)' would fit more here (clearer expresses the intention and SCA friendly).">'string' != gettype($x)</warning>;