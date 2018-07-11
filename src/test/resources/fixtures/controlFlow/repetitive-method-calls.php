<?php

    $object->method()->method();
    <warning descr="Same as in the previous call, consider introducing a local variable instead.">$object->method()</warning>->method();

    for ($i = 0; $i < $max; ++$i) {
         <warning descr="Repetitive call, consider introducing a local variable outside of the loop.">$object->method()</warning>->method();
    }

    /* false-positives: variable being used */
    for ($i = 0; $i < $max; ++$i) {
        $object->method($i)->method();
    }

    foreach ([] as $value) {
        <warning descr="Repetitive call, consider introducing a local variable outside of the loop.">$object->method()</warning>->method();
    }

    /* false-positives: variable being used */
    foreach ([] as $key => $value) {
        $object->method($value)->method();
    }
    foreach ([] as $key => $value) {
        $object->method($key)->method();
    }

    $details = [
        'name'  => $source->getUser()->getName(),
        'email' => <warning descr="Same as in the previous call, consider introducing a local variable instead.">$source->getUser()</warning>->getEmail()
    ];
    $details = array(
        $source->getUser()->getName(),
        <warning descr="Same as in the previous call, consider introducing a local variable instead.">$source->getUser()</warning>->getEmail()
    );

    if (
        $object->method()->one() &&
        <warning descr="Same as in the previous call, consider introducing a local variable instead.">$object->method()</warning>->two()
    ) {}
    if (
        $object->method()->one() ||
        <warning descr="Same as in the previous call, consider introducing a local variable instead.">$object->method()</warning>->two()
    ) {}