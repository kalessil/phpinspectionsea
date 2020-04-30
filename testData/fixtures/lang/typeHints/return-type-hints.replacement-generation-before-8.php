<?php

namespace RootNamespace {
    class CasesHolder {
        /** @return static */
        abstract public function methodReturnsStatic();
        /** @return $this */
        abstract public function <weak_warning descr="[EA] ': self' can be declared as return type hint.">methodReturnsThis</weak_warning>();
    }
}
