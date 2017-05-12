<?php

try {
    if ($x > 0) {
        return $x;
    }
} catch (Exception $e) {
} finally {
    <error descr="Overrides return/throw statements from the try-block">return -1;</error>
}

try {
    if ($x > 0) {
        throw new RuntimeException();
    }
} catch (Exception $e) {
} finally {
    <error descr="Overrides return/throw statements from the try-block">return -1;</error>
}