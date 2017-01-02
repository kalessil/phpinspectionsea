<?php

    $x = intval(1 / 2, 16);

    $x = <error descr="'(int) ...' construction should be used instead (up to 6x times faster).">intval($y)</error>;
    $x = <error descr="'(float) ...' construction should be used instead (up to 6x times faster).">floatval($y)</error>;
    $x = <error descr="'(string) ...' construction should be used instead (up to 6x times faster).">strval($y)</error>;
    $x = <error descr="'(bool) ...' construction should be used instead (up to 6x times faster).">boolval($y)</error>;
    <error descr="'(<needed type>) ...' construction should be used instead (up to 6x times faster).">settype</error>($x, 'whatever');

    settype($x, $x);