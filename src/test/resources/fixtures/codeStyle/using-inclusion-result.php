<?php

require '...';
include '...';

class CasesHolder {
    public function one() {
        return <weak_warning descr="Operating on this return mechanism is considered a bad practice. OOP can be used instead.">require('...')</weak_warning>;
        return <weak_warning descr="Operating on this return mechanism is considered a bad practice. OOP can be used instead.">include('...')</weak_warning>;
    }

    public function two($object) {
        $local = <weak_warning descr="Operating on this return mechanism is considered a bad practice. OOP can be used instead.">require '...'</weak_warning>;
        return [
            call(<weak_warning descr="Operating on this return mechanism is considered a bad practice. OOP can be used instead.">require '...'</weak_warning>),
            $object->call(<weak_warning descr="Operating on this return mechanism is considered a bad practice. OOP can be used instead.">require '...'</weak_warning>),
            false === <weak_warning descr="Operating on this return mechanism is considered a bad practice. OOP can be used instead.">require '...'</weak_warning>,
            $object['...'] = <weak_warning descr="Operating on this return mechanism is considered a bad practice. OOP can be used instead.">require '...'</weak_warning>,
        ];
    }
}