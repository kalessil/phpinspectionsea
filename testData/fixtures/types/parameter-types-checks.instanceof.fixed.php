<?php

interface CasesHolderParent {}

class CasesHolder implements CasesHolderParent
{
    public function first(string $string, stdClass $object, CasesHolderParent $self) {
        return [
            $string instanceof CasesHolderParent,
            $object instanceof CasesHolderParent,
            $self instanceof CasesHolderParent,

            $self instanceof CasesHolder,
        ];
    }

    public function seconds(?CasesHolderParent $first, CasesHolderParent $second = null) {
        return [
            $first !== null,
            $first === null,

            $second !== null,
            $second === null,
        ];
    }
}