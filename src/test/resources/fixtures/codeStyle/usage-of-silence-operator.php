<?php

namespace {
    <warning descr="Try to avoid using the @, as it hides problems and complicates troubleshooting.">@</warning>touch('...');
    echo <warning descr="Try to avoid using the @, as it hides problems and complicates troubleshooting.">@</warning>$undefinedVar;
}

namespace util {
    function remove_file($file) {}

    class BonusCalculation{
        public static function calculate() {}
    }
}

namespace Test {
    use util\BonusCalculation;
    use function util\remove_file as unlink;

    <warning descr="Try to avoid using the @, as it hides problems and complicates troubleshooting.">@</warning>unlink('...');
    <warning descr="Try to avoid using the @, as it hides problems and complicates troubleshooting.">@</warning>BonusCalculation::calculate();
}

namespace {
    /* false positives: legit successions, white-list */
    fclose(...$args);
    ldap_unbind(...$args);
    ldap_free_result(...$args);
    sqlite_close(...$args);
    mysql_close(...$args);
    mysqli_close(...$args);
    pg_close(...$args);
    filesize(...$args);
    filemtime(...$args);
    unlink(...$args);
    rmdir(...$args);
    chmod(...$args);
    file_exists(...$args);
    posix_isatty(...$args);
    class_exists(...$args);
    get_resource_type(...$args);
    getenv(...$args);
    trigger_error(...$args);

    /* false positives: legit context */
    $result = @mkdir('...');
    (function() { return @mkdir('...'); })();
    $result = false === @mkdir('...');
    $result = false !== @mkdir('...');
    if (!@mkdir('...')) { $result = false; }
    if (@mkdir('...'))  { $result = true;  }
}
