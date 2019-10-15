<?php

function unused_goto_holder($param) {
    usedLabel:
    <weak_warning descr="[EA] The label is not used.">unusedLabel:</weak_warning>

    if ($param) {
        goto usedLabel;
    }
}