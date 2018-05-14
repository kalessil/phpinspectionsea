<?php

    isset(
        <weak_warning descr="This argument can be skipped (handled by its' nested element access).">$array</weak_warning>,
        <weak_warning descr="This argument can be skipped (handled by its' nested element access).">$array[0]</weak_warning>,
        $array[0][0]
    );

    isset(
        <weak_warning descr="This argument can be skipped (handled by its' nested element access).">$array</weak_warning>,
        $array[0][0],
        <weak_warning descr="This argument can be skipped (handled by its' nested element access).">$array[0]</weak_warning>
    );

    isset(
        $array[0][0],
        <weak_warning descr="This argument can be skipped (handled by its' nested element access).">$array</weak_warning>,
        <weak_warning descr="This argument can be skipped (handled by its' nested element access).">$array[0]</weak_warning>
    );

    isset(
        $array[0]->property[0]->property,
        <weak_warning descr="This argument can be skipped (handled by its' nested element access).">$array[0]->property[0]</weak_warning>,
        <weak_warning descr="This argument can be skipped (handled by its' nested element access).">$array[0]->property</weak_warning>,
        <weak_warning descr="This argument can be skipped (handled by its' nested element access).">$array[0]</weak_warning>
    );
