<?php

class NullInitParent {
    protected $overriddenProperty = '';

    private $overriddenNullableProperty1 = '';

    protected $overriddenNullableProperty2;
    protected $overriddenNullableProperty3
        = <weak_warning descr="Null assignment can be safely removed. Define null in annotations if it's important.">null</weak_warning>;
}

class NullInitChild extends NullInitParent {
    protected $overriddenProperty = null;

    private $overriddenNullableProperty1 // parent is not null, but it's a private one
        = <weak_warning descr="Null assignment can be safely removed. Define null in annotations if it's important.">null</weak_warning>;

    protected $overriddenNullableProperty2
        = <weak_warning descr="Null assignment can be safely removed. Define null in annotations if it's important.">null</weak_warning>;
    protected $overriddenNullableProperty3
        = <weak_warning descr="Null assignment can be safely removed. Define null in annotations if it's important.">null</weak_warning>;
}