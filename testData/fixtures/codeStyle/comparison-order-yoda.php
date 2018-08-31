<?php

/* operators check */
$x = <weak_warning descr="Yoda conditions style should be used instead.">$y == ''</weak_warning>;
$x = <weak_warning descr="Yoda conditions style should be used instead.">$y != ''</weak_warning>;
$x = <weak_warning descr="Yoda conditions style should be used instead.">$y === ''</weak_warning>;
$x = <weak_warning descr="Yoda conditions style should be used instead.">$y !== ''</weak_warning>;

/* expressions type */
$x = <weak_warning descr="Yoda conditions style should be used instead.">$y == ''</weak_warning>;
$x = <weak_warning descr="Yoda conditions style should be used instead.">$y == __DIR__</weak_warning>;
$x = <weak_warning descr="Yoda conditions style should be used instead.">$y == 0</weak_warning>;

/* false positives */
$x = '' === $y;
$x = __DIR__ ==  '';
$x = 0 == '';