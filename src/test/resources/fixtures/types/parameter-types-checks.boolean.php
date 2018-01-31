<?php

class CasesHolder {
    public function method(?bool $first, bool $second = null, string $third) {
        return [
            /* correct usage */
            $first === true,
            $first !== true,
            $second === false,
            $second !== false,

            /* target cases */
            <warning descr="Makes no sense, because this type is not defined in annotations.">$third === false</warning>,
            <warning descr="Makes no sense, because it's always true according to annotations.">$third !== false</warning>,

            /* weakly typed comparison is not handled */
            $third == false,
            $third != false,
        ];
    }
}