<?php

class PropertyClass1 {
    public $isPublic;
    protected $isProtected;
    private $isPrivate;
}

final class PropertyClass2 {
    public $isPublic;
    <weak_warning descr="Protected modifier could be replaced by private.">protected</weak_warning> $isProtected;
    private $isPrivate;
}
