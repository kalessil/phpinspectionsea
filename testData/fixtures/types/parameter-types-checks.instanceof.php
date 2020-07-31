<?php

interface CasesHolderParent {}

class CasesHolder implements CasesHolderParent
{
    public function first(string $string, callable $callback, stdClass $object, CasesHolderParent $self) {
        return [
            <warning descr="[EA] It seems to be always false (no object types).">$string instanceof CasesHolderParent</warning>,
            <warning descr="[EA] It seems to be always false (no object types).">$callback instanceof CasesHolderParent</warning>,
            <warning descr="[EA] It seems to be always false (classes are not related).">$object instanceof CasesHolderParent</warning>,
            <warning descr="[EA] It seems to be always true (same object type).">$self instanceof CasesHolderParent</warning>,

            $self instanceof CasesHolder,
            $callback instanceof \Closure,
        ];
    }

    public function seconds(?CasesHolderParent $first, CasesHolderParent $second = null) {
        return [
            <warning descr="[EA] '$first !== null' can be used instead.">$first instanceof CasesHolderParent</warning>,
            <warning descr="[EA] '$first === null' can be used instead.">! $first instanceof CasesHolderParent</warning>,

            <warning descr="[EA] '$second !== null' can be used instead.">$second instanceof CasesHolderParent</warning>,
            <warning descr="[EA] '$second === null' can be used instead.">! $second instanceof CasesHolderParent</warning>,
        ];
    }
}