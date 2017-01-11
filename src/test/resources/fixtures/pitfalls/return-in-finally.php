<?php

try {
    if ($x > 0) {
        return $x;
    }
} catch (Exception $e) {

} finally {
    <error descr="Overrides returned values from the try-block">return -1;</error>
}