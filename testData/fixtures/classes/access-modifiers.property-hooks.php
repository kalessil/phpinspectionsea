<?php

class ClassWithPropertyHooks {
    public bool $property1 {
        get => true;
        set => true;
    }

    public(set) bool $property2 {
        get => true;
        set => true;
    }

    protected(set) bool $property3 {
        get => true;
        set => true;
    }

    private(set) bool $property4 {
        get => true;
        set => true;
    }
}