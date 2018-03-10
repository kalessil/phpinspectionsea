<?php

foreach ([] as $value) {}

foreach ([] as & $value) {}

foreach ([] as $value) {}

/* @var $array string[] */
$array = [];
foreach ($array as $value) {}

$array = require $file;
foreach ($array as $value) {}