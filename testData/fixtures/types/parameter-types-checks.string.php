<?php

class CasesHolder {
    public function method(?string $first, string $second = null, array $third) {
        return [
            /* correct usage */
            $first === '',
            $first !== '',
            $second === '',
            $second !== '',

            /* target cases */
            <warning descr="[EA] Makes no sense, because it's always false according to resolved type. Ensure the parameter is not reused.">$third === ''</warning>,
            <warning descr="[EA] Makes no sense, because it's always true according to resolved type. Ensure the parameter is not reused.">$third !== ''</warning>,

            /* weakly typed comparison is not handled */
            $third == '',
            $third != '',
        ];
    }
}