<?php

function cases_holder() {
    $content = <warning descr="[EA] 'ob_get_clean()' can be used instead.">ob_get_contents()</warning>;
    ob_end_clean();

    $content = trim(<warning descr="[EA] 'ob_get_clean()' can be used instead.">ob_get_contents()</warning>);
    ob_end_clean();
}