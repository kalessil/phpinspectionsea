<?php

function cases_holder(\PDO $x)
{
    $y = $x->prepare('');
    /** DocBlock should not break inspection */
    /* comment should not break inspection */
    /** multiple DocBlocks should not break inspection */
    <weak_warning descr="[EA] 'PDO::query(...)' should be used instead of 'prepare-execute' calls chain.">$y->execute()</weak_warning>;

    // Should not be reported, see https://github.com/doctrine/dbal/pull/3200#discussion_r663412391
    $x->query('...');

    /* false-positives: parameters */
    $z = $x->prepare('');
    $z->execute([]);
}