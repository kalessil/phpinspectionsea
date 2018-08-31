<?php

/** @return null|bool */
function functions_not_supported() { return null; }

interface InterfaceCasesHolder {
    /* case 1: handling of abstract methods */
    /** @return null|string */
    function <weak_warning descr="': ?string' can be declared as return type hint.">definitionWithDocBlock</weak_warning>();
    function definitionWithoutDocBlock();
}

abstract class ClassCasesHolder {
    /* case 1: handling of abstract methods */
    /** @return null|string */
    protected abstract function <weak_warning descr="': ?string' can be declared as return type hint.">abstractMethodWithDocBlock</weak_warning>();
    protected abstract function abstractMethodWithoutDocBlock();

    /* false-positives: magic methods */
    public function __toString()       { return ''; }

    /* false-positives: returning mixed and objects */
    /** @param $x mixed */
    public function returnMixed($x)    { return $x; }
    /** @param $x object */
    public function returnObject($x)   { return $x; }

    /* case 2: handling supported types */
    /** @param $x stdClass */
    public function <weak_warning descr="': \stdClass' can be declared as return type hint.">returnClass</weak_warning>($x) { return $x; }
    /** @param $x void */
    public function <weak_warning descr="': void' can be declared as return type hint.">returnVoid</weak_warning>($x) { return $x; }
    /** @param $x null */
    public function returnNull($x) { return $x; }
    /** @param $x callable */
    public function <weak_warning descr="': callable' can be declared as return type hint.">returnCallable</weak_warning>($x) { return $x; }
    /** @param $x array */
    public function <weak_warning descr="': array' can be declared as return type hint.">returnArray</weak_warning>($x) { return $x; }
    /** @param $x bool */
    public function <weak_warning descr="': bool' can be declared as return type hint.">returnBool</weak_warning>($x) { return $x; }
    /** @param $x float */
    public function <weak_warning descr="': float' can be declared as return type hint.">returnFloat</weak_warning>($x) { return $x; }
    /** @param $x int */
    public function <weak_warning descr="': int' can be declared as return type hint.">returnInt</weak_warning> ($x){ return $x; }
    /** @param $x string */
    public function <weak_warning descr="': string' can be declared as return type hint.">returnString</weak_warning>($x) { return $x; }

    /* case 3: handling supported nullable types */
    /** @param $x stdClass|null */
    public function <weak_warning descr="': ?\stdClass' can be declared as return type hint.">returnClassN</weak_warning>($x) { return $x; }
    /** @param $x void|null */
    public function <weak_warning descr="': void' can be declared as return type hint.">returnVoidN</weak_warning>($x) { return $x; }
    /** @param $x callable|null */
    public function <weak_warning descr="': ?callable' can be declared as return type hint.">returnCallableN</weak_warning>($x) { return $x; }
    /** @param $x array|null */
    public function <weak_warning descr="': ?array' can be declared as return type hint.">returnArrayN</weak_warning>($x) { return $x; }
    /** @param $x bool|null */
    public function <weak_warning descr="': ?bool' can be declared as return type hint.">returnBoolN</weak_warning>($x) { return $x; }
    /** @param $x float|null */
    public function <weak_warning descr="': ?float' can be declared as return type hint.">returnFloatN</weak_warning>($x) { return $x; }
    /** @param $x int|null */
    public function <weak_warning descr="': ?int' can be declared as return type hint.">returnIntN</weak_warning>($x) { return $x; }
    /** @param $x string|null */
    public function <weak_warning descr="': ?string' can be declared as return type hint.">returnStringN</weak_warning>($x) { return $x; }

    /* case 4: handling supported nullable types; no DocBlock, incomplete returns structure; */
    public function <weak_warning descr="': ?\stdClass' can be declared as return type hint.">NonImplicitNullReturn</weak_warning>($x) {
        if ($x) {
            return new \stdClass();
        }
    }

    /* case: QF injection */
    /** @param $x string */
    public function <weak_warning descr="': string' can be declared as return type hint (please use change signature intention to fix this).">overriddenReturnString</weak_warning>($x) { return $x; }

    /* false-positive: mixed types leads to no warning */
    private function mixedReturnType($input) {
        $result = json_decode($input);
        return $result['property'];
    }
}

class ClassCasesHolderImplementation extends ClassCasesHolder {
    public function <weak_warning descr="': string' can be declared as return type hint (please use change signature intention to fix this).">overriddenReturnString</weak_warning>($x) { return $x; }
}

class GeneratorCaseHolder {
    public function generator($string)
    {
        if ($string != null) {
            return trim(yield $string);
        }
    }
}