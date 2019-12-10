<?php

    <warning descr="Perhaps it should be used 'opcache_compile_file()' here. See https://bugs.php.net/bug.php?id=78918 for details.">require_once '...'</warning>;
    <warning descr="Perhaps it should be used 'opcache_compile_file()' here. See https://bugs.php.net/bug.php?id=78918 for details.">require '...'</warning>;
    <warning descr="Perhaps it should be used 'opcache_compile_file()' here. See https://bugs.php.net/bug.php?id=78918 for details.">include_once '...'</warning>;
    <warning descr="Perhaps it should be used 'opcache_compile_file()' here. See https://bugs.php.net/bug.php?id=78918 for details.">include '...'</warning>;

    $configuration = (require '...');