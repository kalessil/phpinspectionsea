<?php

    $source = [];
    $result = [];
    foreach ([] as $v) {
        $result[trim($v)]             = trim($v);
        $result[(trim($v))]           = trim($v);
        $result['prefix_' . trim($v)] = trim($v);
        $result['source_' . trim($v)] = $source[trim($v)];
    }