<?php

namespace RootNamespace {
    use \RootNamespace\ChildNamespace\One;
    use \RootNamespace\ChildNamespace\Two as Alias;

    class CasesHolder {
        /** @return \RootNamespace\ChildNamespace\One|null */
        abstract public function <weak_warning descr="': ?One' can be declared as return type hint.">methodOne</weak_warning>();

        /** @return \RootNamespace\ChildNamespace\Two|null */
        abstract public function <weak_warning descr="': ?Alias' can be declared as return type hint.">methodTwo</weak_warning>();

        /** @return \RootNamespace\ChildNamespace\Three|null */
        abstract public function <weak_warning descr="': ?ChildNamespace\Three' can be declared as return type hint.">methodThree</weak_warning>();

        /** @return \VendorNamespace\VendorComponent|null */
        abstract public function <weak_warning descr="': ?\VendorNamespace\VendorComponent' can be declared as return type hint.">methodVendor</weak_warning>();
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