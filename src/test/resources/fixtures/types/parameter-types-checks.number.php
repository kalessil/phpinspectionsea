<?php

class CasesHolder {
    public function method(?int $first, float $second = null, string $third) {
        return [
            /* correct usage */
            $first === 0,
            $first !== 0,
            $second === 0,
            $second !== 0,

            /* target cases */
            <warning descr="Makes no sense, because this type is not defined in annotations.">$third === 0</warning>,
            <warning descr="Makes no sense, because this type is not defined in annotations.">$third === .0</warning>,
            <warning descr="Makes no sense, because this type is not defined in annotations.">$third === 0.0</warning>,
            /* target cases */
            <warning descr="Makes no sense, because it's always true according to annotations.">$third !== 0.0</warning>,
            <warning descr="Makes no sense, because it's always true according to annotations.">$third !== .0</warning>,
            <warning descr="Makes no sense, because it's always true according to annotations.">$third !== 0</warning>,

            /* weakly typed comparison is not handled */
            $third == 0,
            $third != 0,
        ];
    }
}