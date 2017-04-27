<?php

class ClassWithProperty
{
    public $property;
}

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
        $regularObject = new ClassWithProperty();
        $validObject   = new ClassWithIsset();
        $invalidObject = new ClassWithoutIsset();
        return [
            isset($regularObject->property),
            isset($validObject->property),
            isset(<error descr="\ClassWithoutIsset needs to implement __isset to properly work here.">$invalidObject->property</error>),

            empty($regularObject->property),
            empty($validObject->property),
            empty(<error descr="\ClassWithoutIsset needs to implement __isset to properly work here.">$invalidObject->property</error>)
        ];
    }
}