<?php

function cases_holder() {
    $content = ob_get_contents();
    ob_end_clean();

    $content = trim(ob_get_contents());
    ob_end_clean();
}