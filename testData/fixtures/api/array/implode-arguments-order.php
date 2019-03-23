<?php

function cases_holder() {
    return [
        <weak_warning descr="The glue argument should be the first one.">implode([], ',')</weak_warning>,

        implode(),
        implode([]),
        implode(',', []),
    ];
}