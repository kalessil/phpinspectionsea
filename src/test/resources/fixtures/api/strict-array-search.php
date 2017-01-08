<?php

    <weak_warning descr="Third parameter should be provided to clarify if types safety important in this context.">array_search ('1', [])</weak_warning>;
    <weak_warning descr="Third parameter should be provided to clarify if types safety important in this context.">in_array ('1', [])</weak_warning>;

    /* false-positives */
    array_search('1', [], true);
    in_array('1', [], false);