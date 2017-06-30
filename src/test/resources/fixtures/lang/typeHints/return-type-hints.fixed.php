<?php

/** @return null|bool */
function functions_now_supported(): ?bool
{ return null; }

interface InterfaceCasesHolder {
    /* case 1: handling of abstract methods */
    /** @return null|string */
    function definitionWithDocBlock(): ?string;
    function definitionWithoutDocBlock();
}

abstract class ClassCasesHolder {
    /* case 1: handling of abstract methods */
    /** @return null|string */
    protected abstract function abstractMethodWithDocBlock(): ?string;
    protected abstract function abstractMethodWithoutDocBlock();

    /* false-positives: magic methods */
    public function __magicMethod()    { return ''; }

    /* false-positives: returning mixed and objects */
    /** @param $x mixed */
    public function returnMixed($x)    { return $x; }
    /** @param $x object */
    public function returnObject($x)   { return $x; }

    /* case 2: handling supported types */
    /** @param $x stdClass */
    public function returnClass($x): \stdClass
    { return $x; }
    /** @param $x void */
    public function returnVoid($x): void
    { return $x; }
    /** @param $x null */
    public function returnNull($x): void
    { return $x; }
    /** @param $x callable */
    public function returnCallable($x): callable
    { return $x; }
    /** @param $x array */
    public function returnArray($x): array
    { return $x; }
    /** @param $x bool */
    public function returnBool($x): bool
    { return $x; }
    /** @param $x float */
    public function returnFloat($x): float
    { return $x; }
    /** @param $x int */
    public function returnInt ($x): int
    { return $x; }
    /** @param $x string */
    public function returnString($x): string
    { return $x; }

    /* case 3: handling supported nullable types */
    /** @param $x stdClass|null */
    public function returnClassN($x): ?\stdClass
    { return $x; }
    /** @param $x void|null */
    public function returnVoidN($x): void
    { return $x; }
    /** @param $x callable|null */
    public function returnCallableN($x): ?callable
    { return $x; }
    /** @param $x array|null */
    public function returnArrayN($x): ?array
    { return $x; }
    /** @param $x bool|null */
    public function returnBoolN($x): ?bool
    { return $x; }
    /** @param $x float|null */
    public function returnFloatN($x): ?float
    { return $x; }
    /** @param $x int|null */
    public function returnIntN($x): ?int
    { return $x; }
    /** @param $x string|null */
    public function returnStringN($x): ?string
    { return $x; }

    /* case 4: handling supported nullable types; no DocBlock, incomplete returns structure; */
    public function NonImplicitNullReturn($x): ?\stdClass
    {
        if ($x) {
            return new \stdClass();
        }
    }
}
