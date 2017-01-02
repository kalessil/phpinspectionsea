<?php

    /* fields are not resolved, can be dynamic => no report */
    $std = new stdClass();
    $x = isset($std->property);
    $y = !isset($std->property);
    /* assumption: class has offset support; if not, another inspection wil report this */
    $z = isset($std['property']);

    class ClassWithPublicProperty { public $property; }
    $object = new ClassWithPublicProperty();
    $x = isset(<weak_warning descr="'null !== $object->property' construction should be used instead.">$object->property</weak_warning>);
    $y = !isset(<weak_warning descr="'null === $object->property' construction should be used instead.">$object->property</weak_warning>);