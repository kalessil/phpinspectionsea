<?php


function checkUseCases($strToTest) {
    if (strstr($strToTest, 'smth.'))  { return; }
    if (!strstr($strToTest, 'smth.')) { return; }

    if ($strToTest || strstr($strToTest, 'smth.')) { return; }
    if ($strToTest && strstr($strToTest, 'smth.')) { return; }

    if (strstr($strToTest, 'smth.') === false) { return; }
    if (strstr($strToTest, 'smth.') !== false) { return; }
    /* no warnings here */
    if (strstr($strToTest, 'smth.') == false)  { return; }
    if (strstr($strToTest, 'smth.') != false)  { return; }

    if (false === strstr($strToTest, 'smth.')) { return; }
    if (false !== strstr($strToTest, 'smth.')) { return; }
    /* no warnings here */
    if (false ==  strstr($strToTest, 'smth.')) { return; }
    if (false !=  strstr($strToTest, 'smth.')) { return; }
}