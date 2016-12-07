<?php

/* @var \PDO $x*/
$y = $x->prepare('');
/** DocBlock should not break inspection */
/* comment should not break inspection */
/** multiple DocBlocks should not break inspection */
$y-><weak_warning descr="'->query(...)' or '>exec(...)'  should be used instead of 'prepare-execute' calls chain">execute</weak_warning>();

$y = $x->prepare('');
$y->execute();
/** DocBlock should not trigger false-positives */
/* comment should not break inspection */
/** multiple DocBlocks should not trigger false-positives */
$y->execute();


/* False-positives: parameters */
$y = $x->prepare('');
$y->execute([]);
