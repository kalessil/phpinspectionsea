<?php

namespace {
    class ClazzOne {}
    class ClazzTwo extends ClazzOne{}
    class ClazzThree {}

    abstract class CasesHolder {
        /** @var CasesHolder|null */
        private $property;

        abstract function method();

        public function cases(ClazzOne $one, ClazzTwo $two, ClazzThree $three, string $string) {
            return [
                $this->method(),

                $this->property ?? [],
                $this->property ?? null,

                $one ?? $two,
                $two ?? $one,
                $one ?? $three,
                (string) ($one ?? $three),

                $string ?? null,
            ];
        }
    }
}