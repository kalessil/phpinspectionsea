<?php

/* @var array $files */
/* @var array $processed */
/* @var string $hash */

/* dependencies resolution correctness */
foreach ($files as & $file1) {
    $file1 .= "?$hash";
}
foreach ($files as & $file2) {
    $file2 .= '?hash';
}
foreach ($files as & $file3) {
    $file3 = "$file3?$hash";
}

/* accumulating in external storage, e.g. bulk operations */
foreach ($files as & $file4) {
    $processed []= 'Next file has been processed';
    $processed []= $file4;
}

/* increments/decrements, e.g. counters */
foreach ($files as & $file5) {
    ++$processed; $processed++;
    --$processed; $processed--;
}

/* preg_match|preg_match will introduce new variables in the loop */
foreach ($files as & $file6) {
    preg_match('pattern', $file6, $matched);
    preg_match_all('pattern', $file6, $matchedAll);
    unset($matched, $matchedAll);
}

/* list will introduce new variables in the loop */
foreach ($files as & $file7) {
    preg_match('pattern', $file7, $matched);
    list ($first, $second) = $matched;
    unset($first, $second, $matched);
}

/* different assignments: clone, reassigning variables with variables */
$tomorrow = new DateTime();
foreach ($files as & $file8) {
    $tomorrowInstance = clone $tomorrow;
    $tomorrowBackup   = $tomorrow;
}