<?php

class aClass {
    const PATTERN1          = '%%%d%d';
    static public $pattern2 = '%%%d%d';
    protected $pattern3     = '%%%d%d';

    public function check($arg) {
        $pattern4 = '%%%%%%%d%d';

        echo sprintf($pattern4, $arg);
        echo sprintf($pattern4, $arg, $arg);

        echo sprintf(self::PATTERN1, $arg);
        echo sprintf(self::PATTERN1, $arg, $arg);

        echo sprintf(self::$pattern2, $arg);
        echo sprintf(self::$pattern2, $arg, $arg);

        echo sprintf($this->pattern3, $arg);
        echo sprintf($this->pattern3, $arg, $arg);
    }
}