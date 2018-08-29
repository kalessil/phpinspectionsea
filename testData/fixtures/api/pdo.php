<?php

function casesHolder(\PDO $x)
{
    $y = $x->prepare('');
    /** DocBlock should not break inspection */
    /* comment should not break inspection */
    /** multiple DocBlocks should not break inspection */
    <weak_warning descr="'PDO::query(...)' should be used instead of 'prepare-execute' calls chain.">$y->execute()</weak_warning>;

    <weak_warning descr="'PDO::exec(...)' should be used instead (consumes less resources).">$x->query('...')</weak_warning>;
}

function falsePositivesHolder(\PDO $x)
{
    /* false-positives: parameters */
    $y = $x->prepare('');
    $y->execute([]);
}