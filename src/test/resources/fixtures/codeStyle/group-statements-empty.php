<?php

    <weak_warning descr="Empty group statement">if</weak_warning> ($a)      {}
    <weak_warning descr="Empty group statement">elseif</weak_warning> ($b)  {}
    <weak_warning descr="Empty group statement">else</weak_warning>         {}

    <weak_warning descr="Empty group statement">if</weak_warning> ($a)      {}
    <weak_warning descr="Empty group statement">elseif</weak_warning> ($b)  {}
    else <weak_warning descr="Empty group statement">if</weak_warning> ($c) {}

    <weak_warning descr="Empty group statement">foreach</weak_warning> ($x as $v)    {}
    <weak_warning descr="Empty group statement">for</weak_warning> (;;)              {}
    <weak_warning descr="Empty group statement">while</weak_warning> ($y)            {}
    <weak_warning descr="Empty group statement">do</weak_warning>         {} while($z);