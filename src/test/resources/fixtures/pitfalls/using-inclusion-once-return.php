<?php


    $x = <error descr="Only first call returns proper result. Repetitive calls returning 'true'.">require_once $x</error>;
    $y = <error descr="Only first call returns proper result. Repetitive calls returning 'true'.">require_once $x &&
            <error descr="Only first call returns proper result. Repetitive calls returning 'true'.">require_once $y</error></error>;
    if (<error descr="Only first call returns proper result. Repetitive calls returning 'true'.">require_once $x</error>) {
        return (<error descr="Only first call returns proper result. Repetitive calls returning 'true'.">require_once $x</error>);
    }

    /* false-positives */
    require_once $x;
    return (require __DIR__ . 'semicolons.php');