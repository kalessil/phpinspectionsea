<?php

namespace FirstNamespace {
    class TestClass    {}
    class AliasedClass {}
}

namespace SecondNamespace {
    use FirstNamespace\Testclass;
    use FirstNamespace\AliasedClass as AliasClass;

    return [
        <error descr="::class result and class qualified name are not identical (case mismatch).">TestClass</error>::class,
        <error descr="::class result and class qualified name are not identical (case mismatch).">aliasclass</error>::class,
        AliasClass::class,
        <error descr="::class result and class qualified name are not identical (case mismatch).">SubSpace\subspaceclass</error>::class,
        SubSpace\SubSpaceClass::class,
    ];
}
namespace SecondNamespace\SubSpace {
    class SubSpaceClass {}
}

namespace ThirdNamespace {
    use Firstnamespace\TestClass;

    return [
        <error descr="::class result and class qualified name are not identical (case mismatch).">TestClass</error>::class,
        <error descr="::class result and class qualified name are not identical (case mismatch).">\stdclass</error>::class,
        \stdClass::class
    ];
}