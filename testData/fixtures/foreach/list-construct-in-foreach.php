<?php

    foreach ([] as $item) {
        <weak_warning descr="[EA] foreach (... as list(...)) could be used instead.">list($a, $b) = $item</weak_warning>;

        $a = $item[0];
        /** PhpDoc should not break analysis */
        /** Multiple PhpDocs should not break analysis */
        <weak_warning descr="[EA] Perhaps 'list(...) = $item' can be used instead (check similar statements).">$b = $item[1]</weak_warning>;

        $repack    = [];
        $repack[0] = $item[1];
        $repack[1] = $item[0];
    }