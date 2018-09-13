<?php

namespace ClassExistenceAndRelation {

    class C     {}
    trait T     {}
    interface I {}

    $object = new \stdClass();
    $string = '...';

    return [
        <error descr="This call seems to always return false, please inspect the ::class expression.">class_exists</error> (I::class),
        <error descr="This call seems to always return false, please inspect the ::class expression.">class_exists</error> (T::class),
        class_exists(C::class),

        interface_exists(I::class),
        <error descr="This call seems to always return false, please inspect the ::class expression.">interface_exists</error> (T::class),
        <error descr="This call seems to always return false, please inspect the ::class expression.">interface_exists</error> (C::class),

        <error descr="This call seems to always return false, please inspect the ::class expression.">trait_exists</error> (I::class),
        trait_exists(T::class),
        <error descr="This call seems to always return false, please inspect the ::class expression.">trait_exists</error> (C::class),

        is_subclass_of($object, I::class),
        <error descr="This call seems to always return false, please inspect the ::class expression.">is_subclass_of</error> ($object, T::class),
        is_subclass_of($object, C::class),
        <error descr="This call might work not as expected, please specify the third argument.">is_subclass_of</error> ($string, C::class),

        is_a($object, I::class),
        <error descr="This call seems to always return false, please inspect the ::class expression.">is_a</error> ($object, T::class),
        is_a($object, C::class),
        <error descr="This call might work not as expected, please specify the third argument.">is_a</error> ($string, C::class),
    ];
}