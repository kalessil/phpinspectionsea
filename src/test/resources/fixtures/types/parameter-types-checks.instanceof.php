<?php

interface CasesHolderParent {}

class CasesHolder implements CasesHolderParent
{
    public function first(string $string, stdClass $object, CasesHolderParent $self) {
        return [
            $string instanceof CasesHolderParent, // It seems to be always false (no object types).
            $object instanceof CasesHolderParent, // It seems to be always false (classes are not related).
            $self instanceof CasesHolderParent,   // It seems to be always true (same object type).

            $self instanceof CasesHolder,
        ];
    }

    public function seconds(?CasesHolderParent $first, CasesHolderParent $second = null) {
        return [
            $first instanceof CasesHolderParent,  // '$first !== null' can be used instead.
            ! $first instanceof CasesHolderParent,  // '$first === null' can be used instead.

            $second instanceof CasesHolderParent, // '$second !== null' can be used instead.
            ! $second instanceof CasesHolderParent, // '$second === null' can be used instead.
        ];
    }
}