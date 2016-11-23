<?php

    $x = <weak_warning descr="Positive and negative variants can be skipped: the condition already returns a boolean">$x > 0 ? true : false</weak_warning>;
    $x = <weak_warning descr="Positive and negative variants can be skipped: the condition already returns a boolean">$x > 0 ? false : true</weak_warning>;

    $x = $x > 0 ? true : null;
    $x = is_numeric($x) ? false : true;