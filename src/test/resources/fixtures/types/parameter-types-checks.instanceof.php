<?php

interface CasesHolderParent {}

class CasesHolder implements CasesHolderParent
{
    public function first(string $string, stdClass $object, CasesHolderParent $self) {
        return [
            <warning descr="It seems to be always false (no object types).">$string instanceof CasesHolderParent</warning>,
            <warning descr="It seems to be always false (classes are not related).">$object instanceof CasesHolderParent</warning>,
            <warning descr="It seems to be always true (same object type).">$self instanceof CasesHolderParent</warning>,

            $self instanceof CasesHolder,
        ];
    }

    public function seconds(?CasesHolderParent $first, CasesHolderParent $second = null) {
        return [
            <warning descr="'$first !== null' can be used instead.">$first instanceof CasesHolderParent</warning>,
            <warning descr="'$first === null' can be used instead.">! $first instanceof CasesHolderParent</warning>,

            <warning descr="'$second !== null' can be used instead.">$second instanceof CasesHolderParent</warning>,
            <warning descr="'$second === null' can be used instead.">! $second instanceof CasesHolderParent</warning>,
        ];
    }
}