<?php

class CasesHolder {
    public function method(?CasesHolder $first, CasesHolder $second = null, string $third) {
        return [
            /* correct usage */
            $first === null,
            $first !== null,
            $second === null,
            $second !== null,

            /* target cases */
            <warning descr="[EA] Makes no sense, because it's always false according to resolved type. Ensure the parameter is not reused.">$third === null</warning>,
            <warning descr="[EA] Makes no sense, because it's always true according to resolved type. Ensure the parameter is not reused.">$third !== null</warning>,

            /* weakly typed comparison is not handled */
            $third == null,
            $third != null,
        ];
    }
}