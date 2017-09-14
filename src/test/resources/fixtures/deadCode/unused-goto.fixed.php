<?php

function unused_goto_holder($param) {
    usedLabel:

    if ($param) {
        goto usedLabel;
    }
}