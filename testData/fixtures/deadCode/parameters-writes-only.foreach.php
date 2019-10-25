<?php

function cases_holder() {
    foreach ([] as $unused) {
        <weak_warning descr="[EA] Parameter/variable is overridden, but is never used or appears outside of the scope.">$unused[]</weak_warning> = '...';
        <weak_warning descr="[EA] Parameter/variable is overridden, but is never used or appears outside of the scope.">$unused['...']</weak_warning> = '...';
    }

    foreach ([] as $used) {
        $used['...'] = '...';
        unset($used['...']);
    }
}

function false_positives_holder($object) {
    $used = [];
    foreach ([] as $v) {
        $used[] = '...';
    }
    $object->property += $used;

    foreach ([] as & $array) {
        $array[] = '...';
    }
}