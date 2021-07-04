<?php

function cases_holder(\PDO $x)
{
    $y = $x->query('');
    /** DocBlock should not break inspection */
    /* comment should not break inspection */
    /** multiple DocBlocks should not break inspection */

    // Should not be reported, see https://github.com/doctrine/dbal/pull/3200#discussion_r663412391
    $x->query('...');

    /* false-positives: parameters */
    $z = $x->prepare('');
    $z->execute([]);
}