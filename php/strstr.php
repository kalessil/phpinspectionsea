<?php


function checkUseCases($strToTest) {
    if (strstr($strToTest, 'smth.'))  { return; }
    if (!strstr($strToTest, 'smth.')) { return; }

    if ($strToTest || strstr($strToTest, 'smth.')) { return; }
    if ($strToTest && strstr($strToTest, 'smth.')) { return; }

    if (strstr($strToTest, 'smth.') === false) { return; }
    if (strstr($strToTest, 'smth.') !== false) { return; }
    if (strstr($strToTest, 'smth.') == false)  { return; }
    if (strstr($strToTest, 'smth.') != false)  { return; }

    if (false === strstr($strToTest, 'smth.')) { return; }
    if (false !== strstr($strToTest, 'smth.')) { return; }
    if (false ==  strstr($strToTest, 'smth.')) { return; }
    if (false !=  strstr($strToTest, 'smth.')) { return; }

    if (false === stristr($strToTest, 'smth.')) { return; }
    if (false !== stristr($strToTest, 'smth.')) { return; }
    if (false ==  stristr($strToTest, 'smth.')) { return; }
    if (false !=  stristr($strToTest, 'smth.')) { return; }
}