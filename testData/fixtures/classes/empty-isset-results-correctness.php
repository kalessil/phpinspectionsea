<?php

class ClassWithProperty
{
    public $publicProperty;
    protected $protectedProperty;
    private $privateProperty;
}

class ClassWithIsset
{
    public function __isset($name)       {}
    public function __set($name, $value) {}
    public function __get($name)         {}
}

class ClassWithoutIsset
{
    public function casesHolder(): array
    {
        $regularObject = new ClassWithProperty();
        $validObject   = new ClassWithIsset();
        $invalidObject = new ClassWithoutIsset();
        return [
            // covered by IDE itself: Member has private/protected access
            // isset($regularObject->privateProperty),
            // isset($regularObject->protectedProperty),

            isset($regularObject->publicProperty),
            isset($regularObject->{$magic}),
            isset(ClassWithProperty::${$magic}),

            isset($validObject->property),
            isset($validObject->{$magic}),
            isset(ClassWithIsset::${$magic}),
            isset(<error descr="[EA] \ClassWithoutIsset needs to implement __isset to properly work here.">$invalidObject->property</error>),
            isset($invalidObject->{$magic}),
            isset(ClassWithoutIsset::${$magic}),

            // covered by IDE itself: Member has private/protected access
            // empty($regularObject->privateProperty),
            // empty($regularObject->protectedProperty),

            empty($regularObject->publicProperty),
            empty($regularObject->{$magic}),
            empty(ClassWithProperty::${$magic}),

            empty($validObject->property),
            empty($validObject->{$magic}),
            empty(ClassWithIsset::${$magic}),
            empty(<error descr="[EA] \ClassWithoutIsset needs to implement __isset to properly work here.">$invalidObject->property</error>),
            empty($invalidObject->{$magic}),
            empty(ClassWithoutIsset::${$magic}),
        ];
    }

    public function falsePositivesHolder()
    {
        $stdObject = new \StdClass();
        return isset($stdObject->property);
    }
}