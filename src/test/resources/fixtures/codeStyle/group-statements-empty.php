<?php

    <weak_warning descr="Empty block.">if</weak_warning> ($a)      {}
    <weak_warning descr="Empty block.">elseif</weak_warning> ($b)  {}
    <weak_warning descr="Empty block.">else</weak_warning>         {}

    <weak_warning descr="Empty block.">if</weak_warning> ($a)      {}
    <weak_warning descr="Empty block.">elseif</weak_warning> ($b)  {}
    else <weak_warning descr="Empty block.">if</weak_warning> ($c) {}

    <weak_warning descr="Empty block.">foreach</weak_warning> ($x as $v)    {}
    <weak_warning descr="Empty block.">for</weak_warning> (;;)              {}
    <weak_warning descr="Empty block.">while</weak_warning> ($y)            {}
    <weak_warning descr="Empty block.">do</weak_warning>         {} while($z);