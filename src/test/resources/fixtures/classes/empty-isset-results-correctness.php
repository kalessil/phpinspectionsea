<?php

class ClassWithIsset
{
    public function __isset($name)       { throw new \RuntimeException(); }
    public function __set($name, $value) { throw new \RuntimeException(); }
    public function __get($name)         { throw new \RuntimeException(); }
}

class ClassWithoutIsset
{
    public function casesHolder(): array
    {
        $validObject   = new ClassWithIsset();
        $invalidObject = new ClassWithoutIsset();
        return [
            isset($validObject),
            isset(<error descr="\ClassWithoutIsset needs to implement __isset to properly work here.">$invalidObject</error>),
            empty($validObject),
            empty(<error descr="\ClassWithoutIsset needs to implement __isset to properly work here.">$invalidObject</error>)
        ];
    }
}