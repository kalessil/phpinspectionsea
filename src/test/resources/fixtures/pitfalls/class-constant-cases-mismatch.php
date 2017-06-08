<?php

namespace FirstNamespace {
    class TestClass {}
    class AliasedClass {}
}

namespace AnotherNamespace {
    use FirstNamespace\Testclass;
    use FirstNamespace\AliasedClass as AliasClass;

    return [
        <error descr="::class result and class qualified name are not identical (case mismatch).">TestClass</error>::class,
        <error descr="::class result and class qualified name are not identical (case mismatch).">Aliasclass</error>::class
    ];
}

namespace YetAnotherNamespace {
    use Firstnamespace\TestClass;

    return [
        <error descr="::class result and class qualified name are not identical (case mismatch).">TestClass</error>::class,
        \stdclass::class
    ];
}