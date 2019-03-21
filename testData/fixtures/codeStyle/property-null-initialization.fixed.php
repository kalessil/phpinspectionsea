<?php

class NullInitParent {
    protected $overriddenProperty = '';

    private $overriddenNullableProperty1 = '';

    protected $overriddenNullableProperty2;
    protected $overriddenNullableProperty3;
}

class NullInitChild extends NullInitParent {
    protected $overriddenProperty;

    private $overriddenNullableProperty1;

    protected $overriddenNullableProperty2;
    protected $overriddenNullableProperty3;
}