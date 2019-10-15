<?php

class CasesHolder {
    public function method(?array $first, array $second = null, string $third) {
        return [
            /* correct usage */
            $first === [],
            $first !== [],
            $second === [],
            $second !== [],

            /* target cases */
            <warning descr="[EA] Makes no sense, because this type is not defined in annotations.">$third === []</warning>,
            <warning descr="[EA] Makes no sense, because it's always true according to annotations.">$third !== []</warning>,

            /* weakly typed comparison is not handled */
            $third == [],
            $third != [],
        ];
    }
}