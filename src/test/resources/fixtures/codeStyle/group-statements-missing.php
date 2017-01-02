<?php

    <weak_warning descr="Wrap constructs' body within a block.">if</weak_warning> ($a)      ;
    <weak_warning descr="Wrap constructs' body within a block.">elseif</weak_warning> ($b)  ;
    <weak_warning descr="Wrap constructs' body within a block.">else</weak_warning>         ;

    <weak_warning descr="Wrap constructs' body within a block.">if</weak_warning> ($a)      ;
    <weak_warning descr="Wrap constructs' body within a block.">elseif</weak_warning> ($b)  ;
    else <weak_warning descr="Wrap constructs' body within a block.">if</weak_warning> ($c) ;

    <weak_warning descr="Wrap constructs' body within a block.">foreach</weak_warning> ($x as $v)    ;
    <weak_warning descr="Wrap constructs' body within a block.">for</weak_warning> (;;)              ;
    <weak_warning descr="Wrap constructs' body within a block.">while</weak_warning> ($y)            ;
    <weak_warning descr="Wrap constructs' body within a block.">do</weak_warning>         ; while($z);