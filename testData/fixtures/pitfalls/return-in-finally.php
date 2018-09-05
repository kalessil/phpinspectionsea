<?php

try {
    if ($x > 0) {
        return $x;
    }
} catch (Exception $e) {
} finally {
    <error descr="Voids all return and throw statements from the try-block (returned values and exceptions are lost)">return -1;</error>
}

try {
    if ($x > 0) {
        throw new RuntimeException();
    }
} catch (Exception $e) {
} finally {
    <error descr="Voids all return and throw statements from the try-block (returned values and exceptions are lost)">return -1;</error>
}