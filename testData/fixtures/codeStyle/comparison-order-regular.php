<?php

/* operators check */
$x = <weak_warning descr="Regular conditions style should be used instead.">'' == $y</weak_warning>;
$x = <weak_warning descr="Regular conditions style should be used instead.">'' != $y</weak_warning>;
$x = <weak_warning descr="Regular conditions style should be used instead.">'' === $y</weak_warning>;
$x = <weak_warning descr="Regular conditions style should be used instead.">'' !== $y</weak_warning>;

/* expressions type */
$x = <weak_warning descr="Regular conditions style should be used instead.">'' == $y</weak_warning>;
$x = <weak_warning descr="Regular conditions style should be used instead.">__DIR__ == $y</weak_warning>;
$x = <weak_warning descr="Regular conditions style should be used instead.">0 == $y</weak_warning>;

/* false positives */
$x = $y === '';
$x = __DIR__ == '';
$x = 0 == '';