<?php

sideEffectUnknow();


function sideEffectNone($number = null) {
}

<weak_warning descr="This call can be removed because it have no side-effect.">sideEffectNone();</weak_warning>
<weak_warning descr="This call can be removed because it have no side-effect.">sideEffectNone(1);</weak_warning>

if (sideEffectNone()) {
    <weak_warning descr="This call can be removed because it have no side-effect.">sideEffectNone(sideEffectNone());</weak_warning>
}


function sideEffectPossible($arg1, &$arg2 = null) {
}

<weak_warning descr="This call can be removed because it have no side-effect.">sideEffectPossible(1);</weak_warning>
sideEffectPossible(1, $arg2);

if (sideEffectPossible()) {
    <weak_warning descr="This call can be removed because it have no side-effect.">sideEffectPossible(sideEffectPossible());</weak_warning>
}


function sideEffectExternalResource(resource $resource, &$possible = null) {
}

sideEffectExternalResource();
sideEffectExternalResource($resource);

$shouldNotConsider = function () {
};
$shouldNotConsider();


/** No annotation */
function noAnnotation() {
}

<weak_warning descr="Unsupported value on property @side-effect.">/**
 * @side-effect invalid
 */</weak_warning>
function invalidMultilineAnnotation(){
}

<weak_warning descr="Unsupported value on property @side-effect.">/** @side-effect invalid */</weak_warning>
function invalidSinglelineAnnotation(){
}

<weak_warning descr="Multiple declarations of @side-effect is not allowed.">/**
 * @side-effect none
 * @side-effect external
 */</weak_warning>
function invalidMultipleAnnotations(){
}


/**
 * Emulating a PHP internal function.
 * @side-effect external
 */
function sleep() {
}
sleep();

function sumSideEffectNone($a, $b) {
    return $a + $b;
}
<weak_warning descr="This call can be removed because it have no side-effect.">sumSideEffectNone();</weak_warning>


function sumSideEffectExternal() {
    return touch();
}
sumSideEffectExternal();


function sumSideEffectUnknow() {
    return unresolvedFunction();
}
sumSideEffectUnknow();


function factorial(){
    return factorial();
}
<weak_warning descr="This call can be removed because it have no side-effect.">factorial();</weak_warning>


class SideEffectNone_WithConstructor {
    public function __construct() {}
}
<weak_warning descr="This call can be removed because it have no side-effect.">new SideEffectNone_WithConstructor;</weak_warning>
<weak_warning descr="This call can be removed because it have no side-effect.">$sideEffectNone_WithoutConstructor = new SideEffectNone_WithConstructor;</weak_warning>


class SideEffectNone_WithoutConstructor {}
<weak_warning descr="This call can be removed because it have no side-effect.">new SideEffectNone_WithoutConstructor;</weak_warning>
<weak_warning descr="This call can be removed because it have no side-effect.">$sideEffectNone_WithoutConstructor = new SideEffectNone_WithoutConstructor;</weak_warning>
echo new SideEffectNone_WithoutConstructor;


class SideEffect_FullExample {
    public function calculate() { }
}
<weak_warning descr="This call can be removed because it have no side-effect.">$sideEffect_FullExample = new SideEffect_FullExample;</weak_warning>
$sideEffect_FullExample->calculate();

$sideEffect_FullExample_Echoed = new SideEffect_FullExample;
echo $sideEffect_FullExample_Echoed->calculate();


(new stdClass)->field = new SideEffect_FullExample();
