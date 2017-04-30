<?php

function casesHolder()
{
    /* @var \PDO $x*/
    $y = $x->prepare('');
    /** DocBlock should not break inspection */
    /* comment should not break inspection */
    /** multiple DocBlocks should not break inspection */
    <weak_warning descr="'PDO::query(...)' should be used instead of 'prepare-execute' calls chain.">$y->execute()</weak_warning>;
}

function falsePositivesHolder()
{
    /* false-positives: parameters */
    $y = $x->prepare('');
    $y->execute([]);
}