<?php
    $x = 0;
    $x = gettype($x) === 'unknown type';

    $x = gettype($x) === <error descr="'oops' is not a value returned by gettype(...)">'oops'</error>;
    $x = <warning descr="'is_float($x)' construction would be more compact and easier to read">gettype($x) === 'double'</warning>;
    $x = <warning descr="'is_int($x)' construction would be more compact and easier to read">gettype($x) === 'integer'</warning>;

    $x = <warning descr="'is_string($x)' construction would be more compact and easier to read">gettype($x) === 'string'</warning>;
    $x = <warning descr="'!is_string($x)' construction would be more compact and easier to read">gettype($x) !== 'string'</warning>;
    $x = <warning descr="'is_string($x)' construction would be more compact and easier to read">'string' === gettype($x)</warning>;
    $x = <warning descr="'!is_string($x)' construction would be more compact and easier to read">'string' !== gettype($x)</warning>;

    $x = <warning descr="'is_string($x)' construction would be more compact and easier to read">gettype($x) == 'string'</warning>;
    $x = <warning descr="'!is_string($x)' construction would be more compact and easier to read">gettype($x) != 'string'</warning>;
    $x = <warning descr="'is_string($x)' construction would be more compact and easier to read">'string' == gettype($x)</warning>;
    $x = <warning descr="'!is_string($x)' construction would be more compact and easier to read">'string' != gettype($x)</warning>;