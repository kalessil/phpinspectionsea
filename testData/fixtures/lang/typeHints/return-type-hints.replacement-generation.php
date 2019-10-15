<?php

namespace RootNamespace {
    use \RootNamespace\ChildNamespace\One;
    use \RootNamespace\ChildNamespace\Two as Alias;

    class CasesHolder {
        /** @return \RootNamespace\ChildNamespace\One|null */
        abstract public function <weak_warning descr="[EA] ': ?One' can be declared as return type hint.">methodOne</weak_warning>();

        /** @return \RootNamespace\ChildNamespace\Two|null */
        abstract public function <weak_warning descr="[EA] ': ?Alias' can be declared as return type hint.">methodTwo</weak_warning>();

        /** @return \RootNamespace\ChildNamespace\Three|null */
        abstract public function <weak_warning descr="[EA] ': ?ChildNamespace\Three' can be declared as return type hint.">methodThree</weak_warning>();

        /** @return \VendorNamespace\VendorComponent|null */
        abstract public function <weak_warning descr="[EA] ': ?\VendorNamespace\VendorComponent' can be declared as return type hint.">methodVendor</weak_warning>();

        /** @return self */
        abstract public function <weak_warning descr="[EA] ': self' can be declared as return type hint.">methodReturnsSelf</weak_warning>();

        /** @return self|null */
        abstract public function <weak_warning descr="[EA] ': ?self' can be declared as return type hint.">methodReturnsSelfOrNull</weak_warning>();

        /** @return $this */
        abstract public function <weak_warning descr="[EA] ': self' can be declared as return type hint.">methodReturnsThis</weak_warning>();

        /** @return $this|null */
        abstract public function <weak_warning descr="[EA] ': ?self' can be declared as return type hint.">methodReturnsThisOrNull</weak_warning>();

        /** @return static */
        abstract public function methodReturnsStatic();

        private $unknownTypeProperty;
        public function unknownTypeProperty() { return $this->unknownTypeProperty; }

        public function unknownTypeParameter($unknownTypeParameter = null) { return $unknownTypeParameter; }

        public function <weak_warning descr="[EA] ': ?\Generator' can be declared as return type hint.">methodReturnsGenerator</weak_warning>() { yield from []; }
    }
}

namespace RootNamespace\ChildNamespace {
    class One   {}
    class Two   {}
    class Three {}
}

namespace VendorNamespace {
    class VendorComponent {}
}