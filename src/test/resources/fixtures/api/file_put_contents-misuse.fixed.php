<?php

    copy($from, $to);
    copy($from, $to);

    /* false-positives: extra flags */
    file_put_contents($to, file_get_contents($from), $flags);
    file_put_contents($to, file_get_contents($from, $useIncludePath));

    /* false-positives: methods */
    file_put_contents($to, $x->file_get_contents($from));
    $x->file_put_contents($to, file_get_contents($from));