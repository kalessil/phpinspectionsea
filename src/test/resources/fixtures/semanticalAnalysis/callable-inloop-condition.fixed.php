<?php

// Allowed.
for ($i = 0, $iMax = count($values); $i < $iMax; $i++) { }
for ($i = 0; ; $i++) {}

// Warning (case #1).
for ($i = 0, $iMax = count($values); $i < $iMax; $i++) {}
for ($i = 0, $iMax = count($values); $iMax >= $i; $i++) {}
for ($i = 0, $j = 0, $iMax = count($values); $iMax >= $i; $i++) {}

// Warning (case #2).
for ($i = 0, $loopsMax = count($values); $loopsMax >= get($i); $i++) {}

// Warning (case #3).
for ($iMax = count($values); $iMax >= $i; $i++) {}
