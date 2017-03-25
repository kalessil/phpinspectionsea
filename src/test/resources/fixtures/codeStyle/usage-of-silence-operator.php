<?php

namespace {
    <weak_warning descr="Try to avoid using the @, as it hides problems and complicates troubleshooting.">@</weak_warning>file_get_contents('...');
    echo <weak_warning descr="Try to avoid using the @, as it hides problems and complicates troubleshooting.">@</weak_warning>$a;
}

namespace util {
    function remove_file($file) {}

    class BonusCalculation
    {
        public static function calculate($value) {}
    }
}

namespace Test {
    use util\BonusCalculation;
    use function util\remove_file as unlink;

    <weak_warning descr="Try to avoid using the @, as it hides problems and complicates troubleshooting.">@</weak_warning>unlink('...');
    <weak_warning descr="Try to avoid using the @, as it hides problems and complicates troubleshooting.">@</weak_warning>BonusCalculation::calculate(44);
}

namespace {
    /* false positives: commonly suppressed patterns; TODO: must be allowed only in certain contexts */
    @unlink('test.php');
    @\unlink('test.php');
    @mkdir('test/');
    @trigger_error('test/');
}
