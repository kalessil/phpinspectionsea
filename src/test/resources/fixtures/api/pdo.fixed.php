<?php

function casesHolder()
{
    /* @var \PDO $x*/
    $y = $x->query('');
    /** DocBlock should not break inspection */
    /* comment should not break inspection */
    /** multiple DocBlocks should not break inspection */
}

function falsePositivesHolder()
{
    /* false-positives: multiple executions */
    $y = $x->prepare('');
    $y->execute();
    /** DocBlock should not trigger false-positives */
    /* comment should not break inspection */
    /** multiple DocBlocks should not trigger false-positives */
    $y->execute();

    /* false-positives: parameters */
    $y = $x->prepare('');
    $y->execute([]);
}