<?php

    function functionWithUnusedLabels($param) {
        usedLabel:
        <weak_warning descr="The label is not used.">unusedLabel:</weak_warning>

        if ($param) {
            goto usedLabel;
        }
    }