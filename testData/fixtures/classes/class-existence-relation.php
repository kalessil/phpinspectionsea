<?php

namespace ClassExistenceAndRelation {

    class C     {}
    trait T     {}
    interface I {}

    $object = new \stdClass();
    $string = '...';

    return [
        <error descr="This call seems to always return false, please inspect the ::class expression.">class_exists(I::class)</error>,
        <error descr="This call seems to always return false, please inspect the ::class expression.">class_exists(T::class)</error>,
        class_exists(C::class),

        interface_exists(I::class),
        <error descr="This call seems to always return false, please inspect the ::class expression.">interface_exists(T::class)</error>,
        <error descr="This call seems to always return false, please inspect the ::class expression.">interface_exists(C::class)</error>,

        <error descr="This call seems to always return false, please inspect the ::class expression.">trait_exists(I::class)</error>,
        trait_exists(T::class),
        <error descr="This call seems to always return false, please inspect the ::class expression.">trait_exists(C::class)</error>,

        is_subclass_of($object, I::class),
        <error descr="This call seems to always return false, please inspect the ::class expression.">is_subclass_of($object, T::class)</error>,
        is_subclass_of($object, C::class),
        is_subclass_of($string, C::class),

        is_a($object, I::class),
        <error descr="This call seems to always return false, please inspect the ::class expression.">is_a($object, T::class)</error>,
        is_a($object, C::class),
        <error descr="This call might work not as expected, please specify the third argument.">is_a($string, C::class)</error>,
    ];
}