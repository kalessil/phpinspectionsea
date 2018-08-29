<?php
class A {

}
class MyTime {
    public function setTime(\DateTime $time){}
}

class <weak_warning descr="Efferent coupling is 2.">B</weak_warning> extends A{
    public function setTime(\DateTime $time){}
}