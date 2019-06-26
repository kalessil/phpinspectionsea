<?php

function cases_holder(\PDO $x)
{
    $y = $x->query('');
    /** DocBlock should not break inspection */
    /* comment should not break inspection */
    /** multiple DocBlocks should not break inspection */

    $x->exec('...');

   /* false-positives: parameters */
    $z = $x->prepare('');
    $z->execute([]);
}