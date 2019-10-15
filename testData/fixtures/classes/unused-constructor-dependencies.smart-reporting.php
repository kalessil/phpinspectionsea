<?php

class ReportingCasesHolder {

    private $one;
    private $two;
    private $three;

    public function __construct($dependency) {
        <weak_warning descr="[EA] Property is used only in constructor, perhaps we are dealing with dead code here.">$this->one</weak_warning> = $dependency;

        <weak_warning descr="[EA] Property is used only in constructor, perhaps we are dealing with dead code here.">$this->two</weak_warning> = new Clazz();
        $this->two->method();

        <weak_warning descr="[EA] Property is used only in constructor, perhaps we are dealing with dead code here.">$this->three</weak_warning> = new Clazz();
        <weak_warning descr="[EA] Property is used only in constructor, perhaps we are dealing with dead code here.">$this->three</weak_warning> = new Clazz();
        $this->three->method();
    }

    public function method() {
    }
}