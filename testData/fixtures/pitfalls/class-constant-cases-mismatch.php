<?php

namespace FirstNamespace {
    class TestClass    {}
    class AliasedClass {}
}

namespace SecondNamespace {
    use FirstNamespace\Testclass;
    use FirstNamespace\AliasedClass as AliasClass;

    return [
        <error descr="[EA] ::class result and the class qualified name are not identical (case mismatch).">TestClass</error>::class,
        <error descr="[EA] ::class result and the class qualified name are not identical (case mismatch).">aliasclass</error>::class,
        AliasClass::class,
        <error descr="[EA] ::class result and the class qualified name are not identical (case mismatch).">SubSpace\subspaceclass</error>::class,
        SubSpace\SubSpaceClass::class,
    ];
}
namespace SecondNamespace\SubSpace {
    use FirstNamespace\TestClass;

    class SubSpaceClass {}

    return [
        TestClass::class
    ];
}

namespace ThirdNamespace {
    use Firstnamespace\TestClass;  /* import single class */
    use SecondNamespace\SubSpace;  /* import from NS      */

    class ClassOne {}
    class ClassTwo extends ClassOne
    {
        public function method() {
            return [
                <error descr="[EA] ::class result and the class qualified name are not identical (case mismatch).">TestClass</error>::class,
                <error descr="[EA] ::class result and the class qualified name are not identical (case mismatch).">\stdclass</error>::class,
                <error descr="[EA] ::class result and the class qualified name are not identical (case mismatch).">SubSpace\subspaceclass</error>::class,
                SubSpace\SubSpaceClass::class,
                \stdClass::class,
                parent::class,
                self::class,
                static::class
            ];
        }
    }
}