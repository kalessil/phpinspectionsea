<?php

namespace {
    class ClazzOne {}
    class ClazzTwo extends ClazzOne{}
    class ClazzThree {}

    abstract class CasesHolder {
        /** @var CasesHolder|null */
        private $property;

        abstract function method();

        public function cases(ClazzOne $one, ClazzTwo $two, ClazzThree $three) {
            return [
                <weak_warning descr="It possible to use '$this->method()' instead (reduces cognitive load).">$this->method() ?? null</weak_warning>,

                <weak_warning descr="Resolved operands types are not complimentary, while they should be ([\CasesHolder, null] vs [array]).">$this->property ?? []</weak_warning>,
                $this->property ?? null,

                $one ?? $two,
                $two ?? $one,
                <weak_warning descr="Resolved operands types are not complimentary, while they should be ([\ClazzOne] vs [\ClazzThree]).">$one ?? $three</weak_warning>,
            ];
        }
    }
}