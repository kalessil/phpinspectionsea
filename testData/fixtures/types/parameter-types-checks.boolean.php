<?php

class CasesHolder {
    public function method(?bool $first, bool $second = null, stdClass $third, bool $boolean) {
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

            is_bool($first),
            is_bool($second),
            <warning descr="Makes no sense, because this type is not defined in annotations.">is_bool($third)</warning>,
            <warning descr="Makes no sense, because of parameter type declaration.">is_bool($boolean)</warning>,
        ];
    }
}