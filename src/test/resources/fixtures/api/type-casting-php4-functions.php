<?php

    $x = intval(1 / 2, 16);

    $x = <error descr="'(int) ...' construction shall be used instead (up to 6x times faster)">intval($y)</error>;
    $x = <error descr="'(float) ...' construction shall be used instead (up to 6x times faster)">floatval($y)</error>;
    $x = <error descr="'(string) ...' construction shall be used instead (up to 6x times faster)">strval($y)</error>;
    $x = <error descr="'(bool) ...' construction shall be used instead (up to 6x times faster)">boolval($y)</error>;
    <error descr="'(<needed type>) ...' construction shall be used instead (up to 6x times faster)">settype</error>($x, 'whatever');

    settype($x, $x);