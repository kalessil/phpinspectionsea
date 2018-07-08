<?php

    /* fields are not resolved, can be dynamic => no report */
    $std = new stdClass();
    $x = isset($std->property);
    $y = !isset($std->property);
    /* assumption: class has offset support; if not, another inspection wil report this */
    $z = isset($std['property']);

    class ClassWithPublicProperty { public $property; }
    $object = new ClassWithPublicProperty();
    $x = <weak_warning descr="'$object->property !== null' construction should be used instead.">isset($object->property)</weak_warning>;
    $y = <weak_warning descr="'$object->property === null' construction should be used instead.">!isset($object->property)</weak_warning>;
