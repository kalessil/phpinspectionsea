<?php

class CasesHolder {
    public function method(?int $first, float $second = null, stdClass $third) {
        return [
            /* correct usage */
            $first === 0,
            $first !== 0,
            $second === 0,
            $second !== 0,

            /* target cases */
            <warning descr="[EA] Makes no sense, because this type is not defined in annotations.">$third === 0</warning>,
            <warning descr="[EA] Makes no sense, because this type is not defined in annotations.">$third === .0</warning>,
            <warning descr="[EA] Makes no sense, because this type is not defined in annotations.">$third === 0.0</warning>,
            /* target cases */
            <warning descr="[EA] Makes no sense, because it's always true according to annotations.">$third !== 0.0</warning>,
            <warning descr="[EA] Makes no sense, because it's always true according to annotations.">$third !== .0</warning>,
            <warning descr="[EA] Makes no sense, because it's always true according to annotations.">$third !== 0</warning>,

            /* weakly typed comparison is not handled */
            $third == 0,
            $third != 0,

            is_int($first),
            <warning descr="[EA] Makes no sense, because this type is not defined in annotations.">is_int($second)</warning>,

            is_float($second),
            <warning descr="[EA] Makes no sense, because this type is not defined in annotations.">is_float($third)</warning>,

            is_numeric($first),
            is_numeric($second),
            <warning descr="[EA] Makes no sense, because this type is not defined in annotations.">is_numeric($third)</warning>,
        ];
    }

    public function false_positives(string $string) {
        return [
            is_numeric($string),
        ];
    }
}