<?php

    $x = <error descr="Only the first call returns the proper/expected result. Subsequent calls will return 'true'.">require_once $x</error>;
    $y = <error descr="Only the first call returns the proper/expected result. Subsequent calls will return 'true'.">require_once $x &&
         <error descr="Only the first call returns the proper/expected result. Subsequent calls will return 'true'.">require_once $y</error></error>;
    if (<error descr="Only the first call returns the proper/expected result. Subsequent calls will return 'true'.">require_once $x</error>) {
        return (<error descr="Only the first call returns the proper/expected result. Subsequent calls will return 'true'.">require_once $x</error>);
    }

    /* false-positives */
    require_once $x;
    return (require __DIR__ . 'semicolons.php');