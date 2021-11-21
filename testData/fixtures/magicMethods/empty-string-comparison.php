<?php

function checkUseCases(string $string, float $float, int $int)
{
    if (<weak_warning descr="[EA] ''' !== $string' would make more sense here (it also slightly faster).">mb_strlen($string)</weak_warning>)  {}
    if (<weak_warning descr="[EA] ''' === $string' would make more sense here (it also slightly faster).">!mb_strlen($string)</weak_warning>) {}
    if (<weak_warning descr="[EA] ''' !== $string' would make more sense here (it also slightly faster).">strlen($string)</weak_warning>)     {}
    if (<weak_warning descr="[EA] ''' === $string' would make more sense here (it also slightly faster).">!strlen($string)</weak_warning>)    {}

    if ($string === null || <weak_warning descr="[EA] ''' !== $string' would make more sense here (it also slightly faster).">strlen($string)</weak_warning>) {}
    if ($string !== null && <weak_warning descr="[EA] ''' !== $string' would make more sense here (it also slightly faster).">strlen($string)</weak_warning>) {}

    if (<weak_warning descr="[EA] ''' === $string' would make more sense here (it also slightly faster).">strlen($string) == 0</weak_warning>)  {}
    if (<weak_warning descr="[EA] ''' !== $string' would make more sense here (it also slightly faster).">strlen($string) != 0</weak_warning>)  {}
    if (<weak_warning descr="[EA] ''' === $string' would make more sense here (it also slightly faster).">strlen($string) === 0</weak_warning>) {}
    if (<weak_warning descr="[EA] ''' !== $string' would make more sense here (it also slightly faster).">strlen($string) !== 0</weak_warning>) {}

    if (<weak_warning descr="[EA] ''' === $string' would make more sense here (it also slightly faster).">0 ==  strlen($string)</weak_warning>) {}
    if (<weak_warning descr="[EA] ''' !== $string' would make more sense here (it also slightly faster).">0 !=  strlen($string)</weak_warning>) {}
    if (<weak_warning descr="[EA] ''' === $string' would make more sense here (it also slightly faster).">0 === strlen($string)</weak_warning>) {}
    if (<weak_warning descr="[EA] ''' !== $string' would make more sense here (it also slightly faster).">0 !== strlen($string)</weak_warning>) {}

    if (<weak_warning descr="[EA] ''' === $string' would make more sense here (it also slightly faster).">strlen($string) < 1</weak_warning>) {}
    if (<weak_warning descr="[EA] ''' !== $string' would make more sense here (it also slightly faster).">strlen($string) >= 1</weak_warning>)  {}
    if (<weak_warning descr="[EA] ''' !== $string' would make more sense here (it also slightly faster).">strlen($string) > 0</weak_warning>) {}

    if (<weak_warning descr="[EA] ''' != $float' would make more sense here (it also slightly faster).">strlen($float) > 0</weak_warning>) {}
    if (<weak_warning descr="[EA] ''' != $int' would make more sense here (it also slightly faster).">strlen($int) > 0</weak_warning>) {}

    /* not yet supported */
    if (1 >  strlen($string)) {}
    if (1 <= strlen($string)) {}
}