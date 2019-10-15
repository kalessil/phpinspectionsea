<?php

    <weak_warning descr="[EA] Statement has empty block.">if</weak_warning> ($a)      {}
    <weak_warning descr="[EA] Statement has empty block.">elseif</weak_warning> ($b)  {}
    <weak_warning descr="[EA] Statement has empty block.">else</weak_warning>         {}

    <weak_warning descr="[EA] Statement has empty block.">if</weak_warning> ($a)      {}
    <weak_warning descr="[EA] Statement has empty block.">elseif</weak_warning> ($b)  {}
    else <weak_warning descr="[EA] Statement has empty block.">if</weak_warning> ($c) {}

    <weak_warning descr="[EA] Statement has empty block.">foreach</weak_warning> ($x as $v)    {}
    <weak_warning descr="[EA] Statement has empty block.">for</weak_warning> (;;)              {}
    <weak_warning descr="[EA] Statement has empty block.">while</weak_warning> ($y)            {}
    <weak_warning descr="[EA] Statement has empty block.">do</weak_warning>         {} while($z);