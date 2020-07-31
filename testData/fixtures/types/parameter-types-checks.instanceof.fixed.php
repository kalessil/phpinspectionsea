<?php

interface CasesHolderParent {}

class CasesHolder implements CasesHolderParent
{
    public function first(string $string, callable $callback, stdClass $object, CasesHolderParent $self) {
        return [
            $string instanceof CasesHolderParent,
            $callback instanceof CasesHolderParent,
            $object instanceof CasesHolderParent,
            $self instanceof CasesHolderParent,

            $self instanceof CasesHolder,
            $callback instanceof \Closure,
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