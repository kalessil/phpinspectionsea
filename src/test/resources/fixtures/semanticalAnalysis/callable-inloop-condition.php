<?php

// Allowed.
for ($i = 0, $iMax = count($values); $i < $iMax; $i++) { }
for ($i = 0; ; $i++) {}

// Warning.
for ($i = 0; <error descr="Avoid callables in loop conditionals for better performance.">$i < count($values)</error>; $i++) {}
for ($i = 0; <error descr="Avoid callables in loop conditionals for better performance.">count($values) >= $i</error>; $i++) {}
