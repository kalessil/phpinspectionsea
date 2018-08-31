<?php
    $x = gettype($x) === 'unknown type';

    /* incorrect type checked */
    $x = gettype($x) === <error descr="'oops' is not a value returned by 'gettype(...)'.">'oops'</error>;
    /* aliasing taken into account */
    $x = <warning descr="'is_float($x)' construction is more compact and easier to read.">gettype($x) === 'double'</warning>;
    $x = <warning descr="'is_int($x)' construction is more compact and easier to read.">gettype($x) === 'integer'</warning>;

    /* all functions handled correctly */
    $x = <warning descr="'is_bool($x)' construction is more compact and easier to read.">gettype($x) === 'boolean'</warning>;
    $x = <warning descr="'is_int($x)' construction is more compact and easier to read.">gettype($x) === 'integer'</warning>;
    $x = <warning descr="'is_float($x)' construction is more compact and easier to read.">gettype($x) === 'double'</warning>;
    $x = <warning descr="'is_string($x)' construction is more compact and easier to read.">gettype($x) === 'string'</warning>;
    $x = <warning descr="'is_array($x)' construction is more compact and easier to read.">gettype($x) === 'array'</warning>;
    $x = <warning descr="'is_object($x)' construction is more compact and easier to read.">gettype($x) === 'object'</warning>;
    $x = <warning descr="'is_resource($x)' construction is more compact and easier to read.">gettype($x) === 'resource'</warning>;
    $x = <warning descr="'is_null($x)' construction is more compact and easier to read.">gettype($x) === 'NULL'</warning>;

    /* comparison operands are handled correctly */
    $x = <warning descr="'is_string($x)' construction is more compact and easier to read.">gettype($x) === 'string'</warning>;
    $x = <warning descr="'!is_string($x)' construction is more compact and easier to read.">gettype($x) !== 'string'</warning>;
    $x = <warning descr="'is_string($x)' construction is more compact and easier to read.">'string' === gettype($x)</warning>;
    $x = <warning descr="'!is_string($x)' construction is more compact and easier to read.">'string' !== gettype($x)</warning>;
    $x = <warning descr="'is_string($x)' construction is more compact and easier to read.">gettype($x) == 'string'</warning>;
    $x = <warning descr="'!is_string($x)' construction is more compact and easier to read.">gettype($x) != 'string'</warning>;
    $x = <warning descr="'is_string($x)' construction is more compact and easier to read.">'string' == gettype($x)</warning>;
    $x = <warning descr="'!is_string($x)' construction is more compact and easier to read.">'string' != gettype($x)</warning>;