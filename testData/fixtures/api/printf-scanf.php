<?php

class aClass {
    const PATTERN1          = '%%%d%d';
    static public $pattern2 = '%%%d%d';
    protected $pattern3     = '%%%d%d';

    public function check($arg, $handle) {
        $pattern4 = '%%%%%%%d%d';
        $pattern5 = '[%[^]]]';
        $pattern6 = '%*s %s';

        /* all function reported */
        echo <error descr="[EA] Amount of expected parameters is 3.">printf</error> ($pattern4, $arg);
        echo <error descr="[EA] Amount of expected parameters is 3.">sprintf</error> ($pattern4, $arg);
        echo <error descr="[EA] Amount of expected parameters is 4.">fprintf</error> ($handle, $pattern4, $arg);
        echo <error descr="[EA] Amount of expected parameters is 4.">sscanf</error> ($arg, $pattern4, $arg);
        echo <error descr="[EA] Amount of expected parameters is 4.">fscanf</error> ($handle, $pattern4, $arg);

        /* sscanf/fscanf with partially provided containers */
        list($first, $second) = <error descr="[EA] Amount of expected parameters is 4.">sscanf</error> ($arg, $pattern4, $first);
        list($first, $second) = <error descr="[EA] Amount of expected parameters is 4.">fscanf</error> ($handle, $pattern4, $first);

        /* test resolving string literal */
        echo <error descr="[EA] Amount of expected parameters is 3.">sprintf</error> (self::PATTERN1, $arg);
        echo <error descr="[EA] Amount of expected parameters is 3.">sprintf</error> (self::$pattern2, $arg);
        echo <error descr="[EA] Amount of expected parameters is 3.">sprintf</error> ($this->pattern3, $arg);
        echo sprintf(<error descr="[EA] Pattern seems to be not valid.">'%'</error>, $arg);


        /* false-positives */
        echo sprintf($pattern4, $arg, $arg);
        echo sprintf($pattern4, $arg, $arg);
        echo sprintf('%d', $arg);
        echo sprintf('%04.d', $arg);
        echo sprintf('%1$d %1$d', $arg);
        echo sprintf("%% %1$'.-9d %1$'.-9d %s", $arg);
        echo sprintf('%% %1$\'.-9d %1$\'.-9d %s', $arg);
        echo sprintf('%f', $arg);
        echo sprintf('%0.2f', $arg);

        /* false-positive: sscanf returning array */
        list($first, $second) = sscanf($arg, $pattern4);
        $values               = sscanf($arg, $pattern4);
        call(sscanf($arg, $pattern4));
        $object->method(sscanf($arg, $pattern4));

        /* false-positive: fscanf returning array */
        list($first, $second) = fscanf($handle, $pattern4);
        $values               = fscanf($handle, $pattern4);
        call(fscanf($handle, $pattern4));
        $object->method(fscanf($handle, $pattern4));

        /* variadic variables */
        printf('%d %d %d', ...$i);

        /* sscanf/fscanf special formats */
        sscanf($arg, $pattern5, $arg);

        /* suppressed placeholders */
        sscanf($arg, $pattern6, $var2);
    }

    public static function test_case_with_modification()
    {
        $string = 'original';
        if(true) {
            $string .= ', now modified: %s';
        }
        return sprintf($string, '...');
    }
}