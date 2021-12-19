<?php
    declare(strict_types=1);

    foreach ([] as $value) ;
    while (true) ;
    do; while (true);
    for (;;) ;

    if (true) ;
    elseif (true) ;
    else ;

    echo 1;<weak_warning descr="[EA] Unnecessary semicolon.">;</weak_warning>
    <weak_warning descr="[EA] Unnecessary semicolon.">;</weak_warning>
?>

<?= $value<weak_warning descr="[EA] Unnecessary semicolon.">;</weak_warning> ?>
<?= $value; $value = ''; ?>
