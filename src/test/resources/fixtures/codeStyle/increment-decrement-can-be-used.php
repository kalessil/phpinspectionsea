<?php

<weak_warning descr="Can be safely replaced with '$x++'.">$x += 1</weak_warning>;
<weak_warning descr="Can be safely replaced with '$x--'.">$x -= 1</weak_warning>;

<weak_warning descr="Can be safely replaced with '$x++'.">$x = $x + 1</weak_warning>;
<weak_warning descr="Can be safely replaced with '$x++'.">$x = 1 + $x</weak_warning>;

<weak_warning descr="Can be safely replaced with '$x--'.">$x = $x - 1</weak_warning>;
$x = 1 - x;

for ($i = 0; $i < 10; <weak_warning descr="Can be safely replaced with '$i++'.">$i = $i + 1</weak_warning>) {}