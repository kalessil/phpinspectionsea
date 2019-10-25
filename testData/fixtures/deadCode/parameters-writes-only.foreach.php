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
    $used1 = [];
    $used2 = [];
    $used3 = [];
    foreach ([] as $v) {
        $used1[] = '...';
        $used2[] = '...';
        $used3[] = '...';
    }
    $object->property += $used1;
    $object['...'] += $used2;
    $object['...']['...'] += $used2;

    foreach ([] as & $array) {
        $array[] = '...';
    }

    foreach ([] as & $array) {
        $array += ['...'];
    }
}