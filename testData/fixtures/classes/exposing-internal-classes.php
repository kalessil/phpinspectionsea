<?php

/** @internal */
class InternalClass {}

class CasesHolder {
    private function privateMethod(InternalClass $parameter): InternalClass {}
    protected function protectedMethod(InternalClass $parameter): InternalClass {}

    public function exposesViaParameter(<warning descr="[EA] Exposes an @internal class, which should not be exposed via public methods.">InternalClass $parameter</warning>) {}
    public function exposesViaReturn(): <warning descr="[EA] Exposes an @internal class, which should not be exposed via public methods.">InternalClass</warning> {}
}