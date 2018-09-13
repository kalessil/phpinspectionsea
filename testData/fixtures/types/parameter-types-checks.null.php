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
            <warning descr="Makes no sense, because this type is not defined in annotations.">$third === null</warning>,
            <warning descr="Makes no sense, because it's always true according to annotations.">$third !== null</warning>,

            /* weakly typed comparison is not handled */
            $third == null,
            $third != null,
        ];
    }
}