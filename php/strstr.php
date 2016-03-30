<?php


function checkStrstrUseCases($strToTest) {
    if (strstr($strToTest, 'smth.'))  { return; }              // <- reported
    if (!strstr($strToTest, 'smth.')) { return; }              // <- reported

    if ($strToTest || strstr($strToTest, 'smth.')) { return; } // <- reported
    if ($strToTest && strstr($strToTest, 'smth.')) { return; } // <- reported

    if (strstr($strToTest, 'smth.') === false) { return; }     // <- reported
    if (strstr($strToTest, 'smth.') !== false) { return; }     // <- reported
    if (strstr($strToTest, 'smth.') == false)  { return; }     // <- reported
    if (strstr($strToTest, 'smth.') != false)  { return; }     // <- reported

    if (false === strstr($strToTest, 'smth.')) { return; }     // <- reported
    if (false !== strstr($strToTest, 'smth.')) { return; }     // <- reported
    if (false ==  strstr($strToTest, 'smth.')) { return; }     // <- reported
    if (false !=  strstr($strToTest, 'smth.')) { return; }     // <- reported

    if (false === stristr($strToTest, 'smth.')) { return; }    // <- reported
    if (false !== stristr($strToTest, 'smth.')) { return; }    // <- reported
    if (false ==  stristr($strToTest, 'smth.')) { return; }    // <- reported
    if (false !=  stristr($strToTest, 'smth.')) { return; }    // <- reported
}