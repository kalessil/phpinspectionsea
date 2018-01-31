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
            <warning descr="Makes no sense, because this type is not defined in annotations.">$third === ''</warning>,
            <warning descr="Makes no sense, because it's always true according to annotations.">$third !== ''</warning>,

            /* weakly typed comparison is not handled */
            $third == '',
            $third != '',
        ];
    }
}