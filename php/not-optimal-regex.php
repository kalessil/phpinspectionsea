<?php

class aClass {
    const REGEX1 = '/regex1$/Z';
    static public $regex2 = '/regex2$/Z';
    protected $regex3 = '/regex3$/Z';

    public function check($arg) {
        $regex4 = '/regex4$/Z';

        preg_match($regex4, $arg);
        preg_match(self::REGEX1, $arg);
        preg_match(self::$regex2, $arg);
        preg_match($this->regex3, $arg);
    }
}

    preg_match('/^start-with/',  '');                 //report
    preg_match('/^starts-with/i', '');                //report
    preg_match('/^start-with$/',  '');                //ok
    preg_match('/^starts-with$/i', '');               //ok

    preg_match('/contains/',  '');                    //report
    preg_match('/contains/i', '');                    //report
    preg_match('/contains$/',  '');                   //ok
    preg_match('/contains$/i', '');                   //ok

    preg_replace('/contains/',  '', '');              //report
    preg_replace('/contains/i', '', '');              //report
    preg_replace('/^contains/',  '', '');             //ok
    preg_replace('/^contains/i', '', '');             //ok

    preg_match_all('/regex/', '');                    //ok
    if (preg_match_all('/regex/', '')) {}             //report
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

    preg_match('/$-presented/D',           '');       //ok
    preg_match('/^-no-[\\$]-occurrence/D', '');       //reported
    preg_match('/^-ignored-$/mD',          '');       //reported

    preg_match('/.-provided/s',            '');       //ok
    preg_match('/no-dot-char/s',           '');       //reported

    preg_match('/.*-report/',              '');       //reported
    preg_match('/.*-report/',              '', $m);   //ok
    preg_match('/report-.*/',              '');       //reported
    preg_replace('/report-.*/',            '', '');   //ok
    preg_match('/report-.*/',              '', $m);   //ok
    preg_replace('/report-.*/',            '', '');   //ok
    preg_match('/.*(no-report)-.*\0/',     '');       //ok
    preg_match('/^.*-no-report/',          '');       //ok
    preg_match('/no-report-.*$/',          '');       //ok

    preg_match('/a-z-provided/i',          '');       //ok
    preg_match('/.{2}-.{2}/i',             '');       //reported

    preg_match('/[seq].../',                    '');  //ok
    preg_match('/[seq][seq].../',               '');  //reported
    preg_match('/[seq][seq]+/',                 '');  //reported
    preg_match('/[seq][seq]*/',                 '');  //reported
    preg_match('/[seq][seq]?/',                 '');  //reported
    preg_match('/[seq]+[seq]*[seq]?[seq]{1,}/', '');  //reported

    preg_match('/[0-9]/',     '');              //reported
    preg_match('/[^0-9]/',    '');              //reported
    preg_match('/[:digit:]/', '');              //reported
    preg_match('/[:word:]/',  '');              //reported
    preg_match('/[^\w]/',     '');              //reported
    preg_match('/[^\s]/',     '');              //reported

    preg_match('/deprecated/e', '');            //reported
    preg_match('/non-existing/Zy', '');         //reported
