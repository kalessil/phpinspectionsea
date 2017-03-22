<?php

namespace {
    <weak_warning descr="Usage of a silence operator.">@</weak_warning>file_get_contents('test.php');
    echo <weak_warning descr="Usage of a silence operator.">@</weak_warning>$a;
}


namespace util {
    function remove_file($file)
    {

    }

}

namespace {

    use function util\remove_file as unlink;

    <weak_warning descr="Usage of a silence operator.">@</weak_warning>unlink('test.php');
}


/** false positive */
@unlink('test.php');
@mkdir('test/');
