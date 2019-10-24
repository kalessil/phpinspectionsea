<?php

function cases_holder() {
    foreach ([] as <weak_warning descr="[EA] Parameter/variable is overridden, but is never used or appears outside of the scope.">$unused</weak_warning>) {
        <weak_warning descr="[EA] Parameter/variable is overridden, but is never used or appears outside of the scope.">$unused[]</weak_warning> = '...';
        <weak_warning descr="[EA] Parameter/variable is overridden, but is never used or appears outside of the scope.">$unused['...']</weak_warning> = '...';
    }

    foreach ([] as $used) {
        $used['...'] = '...';
        unset($used['...']);
    }
}