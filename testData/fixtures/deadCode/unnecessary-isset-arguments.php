<?php

    isset(
        <weak_warning descr="This argument can be skipped (handled by its array access).">$array</weak_warning>,
        <weak_warning descr="This argument can be skipped (handled by its array access).">$array[0]</weak_warning>,
        $array[0][0]
    );

    isset(
        <weak_warning descr="This argument can be skipped (handled by its array access).">$array</weak_warning>,
        $array[0][0],
        <weak_warning descr="This argument can be skipped (handled by its array access).">$array[0]</weak_warning>
    );

    isset(
        $array[0][0],
        <weak_warning descr="This argument can be skipped (handled by its array access).">$array</weak_warning>,
        <weak_warning descr="This argument can be skipped (handled by its array access).">$array[0]</weak_warning>
    );
