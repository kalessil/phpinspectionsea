<?php

class aClass {
    const PATTERN1          = '%%%d%d';
    static public $pattern2 = '%%%d%d';
    protected $pattern3     = '%%%d%d';

    public function check($arg) {
        $pattern4 = '%%%%%%%d%d';

        echo sprintf($pattern4, $arg);            // <- reported
        echo sprintf($pattern4, $arg, $arg);

        echo sprintf(self::PATTERN1, $arg);       // <- reported
        echo sprintf(self::PATTERN1, $arg, $arg);

        echo sprintf(self::$pattern2, $arg);      // <- reported
        echo sprintf(self::$pattern2, $arg, $arg);

        echo sprintf($this->pattern3, $arg);      // <- reported
        echo sprintf($this->pattern3, $arg, $arg);

        echo sprintf('%', $arg);                  // <- reported
        echo sprintf('%d', $arg);
        echo sprintf('%1$d %1$d', $arg);
        echo sprintf("%% %1$'.-9d %1$'.-9d %s", $arg);
        echo sprintf('%% %1$\'.-9d %1$\'.-9d %s', $arg);
    }
}