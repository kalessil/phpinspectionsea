<?php

    $z = <weak_warning descr="Can be replaced with a compared operand">$x == $y ? $x : $y</weak_warning>;
    $z = <weak_warning descr="Can be replaced with a compared operand">$x != $y ? $x : $y</weak_warning>;
    $z = <weak_warning descr="Can be replaced with a compared operand">$x === $y ? $x : $y</weak_warning>;
    $z = <weak_warning descr="Can be replaced with a compared operand">$x !== $y ? $x : $y</weak_warning>;

    $z = $x ? $x : $y;