<?php

    preg_match('no-modifiers', '');             //ok
    preg_match('/Valid/i', '');                 //ok

    preg_match('/^-both-presented-$/m', '');          //ok
    preg_match('/both^-$presented/m', '');            //ok
    preg_match('/no-[^]-or-[\\$]-occurrences/m', ''); //reported
    preg_match('/^-no-[\\$]-occurrence/m', '');       //reported
    preg_match('/no-[^]-occurrence$/m', '');          //reported

    preg_match('/$-presented/D',           '');       //ok
    preg_match('/^-no-[\\$]-occurrence/D', '');       //reported
    preg_match('/^-ignored-$/mD',          '');       //reported

    preg_match('/[0-9]/',     '');              //reported
    preg_match('/[^0-9]/',    '');              //reported
    preg_match('/[:digit:]/', '');              //reported
    preg_match('/[:word:]/',  '');              //reported
    preg_match('/[^\w]/',     '');              //reported
    preg_match('/[^\s]/',     '');              //reported

    preg_match('/deprecated/e', '');            //reported
    preg_match('/non-existing/Zy', '');         //reported
