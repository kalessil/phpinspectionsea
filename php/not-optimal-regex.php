<?php

    preg_match_all('/regex/', '');                    //ok
    if (preg_match_all('/regex/', '')) {}             //report
    if (preg_match_all("/regex$x/", '')) {}           //ok, due to inline var
    preg_quote('/regex/');                            //report

    preg_match('/(.*?)/', '');                        //ok
    preg_match('/<.+?>/', '');                        //reported

    preg_match('no-modifiers', '');                   //ok
    preg_match('/Valid?/i', '');                      //ok

    preg_match('/^-both-presented-$/m', '');          //ok
    preg_match('/both^-$presented/m', '');            //ok
    preg_match('/no-[^]-or-[\\$]-occurrences/m', ''); //reported
    preg_match('/^-no-[\\$]-occurrence/m', '');       //reported
    preg_match('/no-[^]-occurrence$/m', '');          //reported

    preg_match('/.-provided/s',            '');       //ok
    preg_match('/no-dot-char/s',           '');       //reported

    preg_match('/a-z-provided/i',          '');       //ok
    preg_match('/.{2}-.{2}/i',             '');       //reported

    preg_match('/[0-9]/',       '');              //reported
    preg_match('/[^0-9]/',      '');              //reported
    preg_match('/[:digit:]/',   '');              //reported
    preg_match('/[:word:]/',    '');              //reported
    preg_match('/[^\w]/',       '');              //reported
    preg_match('/[^\s]/',       '');              //reported
    preg_match('/[0-9a-zA-Z_]/', '');             //reported

    preg_match('/\R/',     '');                 //reported
