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
            isset($regularObject-><error descr="Member has private access">privateProperty</error>),
            isset($regularObject-><error descr="Member has protected access">protectedProperty</error>),
            isset($regularObject->publicProperty),
            isset($regularObject->{$magic}),
            isset(ClassWithProperty::${$magic}),

            isset($validObject->property),
            isset($validObject->{$magic}),
            isset(ClassWithIsset::${$magic}),
            isset(<error descr="[EA] \ClassWithoutIsset needs to implement __isset to properly work here.">$invalidObject->property</error>),
            isset($invalidObject->{$magic}),
            isset(ClassWithoutIsset::${$magic}),

            empty($regularObject-><error descr="Member has private access">privateProperty</error>),
            empty($regularObject-><error descr="Member has protected access">protectedProperty</error>),
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