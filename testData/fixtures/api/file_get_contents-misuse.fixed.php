<?php

    copy($from, $to);
    copy($from, $to);
    md5_file($from);
    sha1_file($from);
    hash_file('...', $from);
    hash_hmac_file('...', $from, 'key');

    /* false-positives: php streams */
    file_put_contents($to, file_get_contents('php://...'));

    /* false-positives: extra flags */
    file_put_contents($to, file_get_contents($from), $flags);
    file_put_contents($to, file_get_contents($from, $useIncludePath));

    /* false-positives: methods */
    file_put_contents($to, $x->file_get_contents($from));
    $x->file_put_contents($to, file_get_contents($from));