<?php

function cases_holder() {
    $x = <weak_warning descr="[EA] '(int) $y' can be used instead (reduces cognitive load, up to 6x times faster in PHP 5.x).">intval($y)</weak_warning>;
    $x = <weak_warning descr="[EA] '(float) $y' can be used instead (reduces cognitive load, up to 6x times faster in PHP 5.x).">floatval($y)</weak_warning>;
    $x = <weak_warning descr="[EA] '(string) $y' can be used instead (reduces cognitive load, up to 6x times faster in PHP 5.x).">strval($y)</weak_warning>;
    $x = <weak_warning descr="[EA] '(bool) $y' can be used instead (reduces cognitive load, up to 6x times faster in PHP 5.x).">boolval($y)</weak_warning>;

    $x = <weak_warning descr="[EA] '(int) ($y ?? 1)' can be used instead (reduces cognitive load, up to 6x times faster in PHP 5.x).">intval($y ?? 1)</weak_warning>;
    $x = <weak_warning descr="[EA] '(int) ($y ?: 1)' can be used instead (reduces cognitive load, up to 6x times faster in PHP 5.x).">intval($y ?: 1)</weak_warning>;

    $x = <weak_warning descr="[EA] '(int) (1 / 2)' can be used instead (reduces cognitive load, up to 6x times faster in PHP 5.x).">intval(1 / 2, 10)</weak_warning>;
    $x = intval(1 / 2, 16);

    <weak_warning descr="[EA] '$x = (int) $x' can be used instead (reduces cognitive load, up to 6x times faster in PHP 5.x).">settype($x, 'int')</weak_warning>;
    <weak_warning descr="[EA] '$x = (float) $x' can be used instead (reduces cognitive load, up to 6x times faster in PHP 5.x).">settype($x, 'float')</weak_warning>;
    <weak_warning descr="[EA] '$x = (bool) $x' can be used instead (reduces cognitive load, up to 6x times faster in PHP 5.x).">settype($x, 'bool')</weak_warning>;
    <weak_warning descr="[EA] '$x = (string) $x' can be used instead (reduces cognitive load, up to 6x times faster in PHP 5.x).">settype($x, 'string')</weak_warning>;
    <weak_warning descr="[EA] '$x = (array) $x' can be used instead (reduces cognitive load, up to 6x times faster in PHP 5.x).">settype($x, 'array')</weak_warning>;

    settype($x, $x);
    settype($x, 'whatever');

    $x = <weak_warning descr="[EA] '(string) ($y)' would express the intention here better (less types coercion magic).">"{$y}"</weak_warning>;
    $x = <weak_warning descr="[EA] '(string) $y' would express the intention here better (less types coercion magic).">"$y"</weak_warning>;

    $x = " $y";
    $x = "$y ";
    $x = "{$y}{$y}";

    $x = <weak_warning descr="[EA] '(string) $x' would express the intention here better.">$x->__toString()</weak_warning>;
}

class CasesHolder extends \SimpleXMLElement {
    public function __toString()
    {
        return parent::__toString();
    }
}