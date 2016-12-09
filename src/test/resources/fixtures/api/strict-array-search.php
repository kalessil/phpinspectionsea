<?php

    <weak_warning descr="Third parameter shall be provided to clarify if types safety important in this context">array_search</weak_warning> ('1', []);
    <weak_warning descr="Third parameter shall be provided to clarify if types safety important in this context">in_array</weak_warning> ('1', []);

    /* false-positives */
    array_search('1', [], true);
    in_array('1', [], false);