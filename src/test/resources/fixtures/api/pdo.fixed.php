<?php

function casesHolder(\PDO $x)
{
    $y = $x->query('');
    /** DocBlock should not break inspection */
    /* comment should not break inspection */
    /** multiple DocBlocks should not break inspection */

    $x->exec('...');
}

function falsePositivesHolder()
{
    /* false-positives: parameters */
    $y = $x->prepare('');
    $y->execute([]);
}