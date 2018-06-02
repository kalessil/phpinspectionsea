<?php

    password_hash('...');
    password_hash(<warning descr="It would be better idea to not hard-code credentials but to use a managed environment instead.">'...'</warning>, 'algorithm');

    /* @var ZipArchive $zip */
    $zip->setEncryptionIndex('...', '...', <warning descr="It would be better idea to not hard-code credentials but to use a managed environment instead.">'...'</warning>);

    $connection = new PDO('...', '...', <warning descr="It would be better idea to not hard-code credentials but to use a managed environment instead.">'...'</warning>);