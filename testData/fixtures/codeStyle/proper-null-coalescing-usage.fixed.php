<?php

namespace {
    class ClazzOne {}
    class ClazzTwo extends ClazzOne{}
    class ClazzThree {}

    abstract class CasesHolder {
        const DEFAULT_LIFETIME = 3600 * 24;

        /** @var CasesHolder|null */
        private $property;

        abstract function method();

        public function cases(ClazzOne $one, ClazzTwo $two, ClazzThree $three, string $string = null, int $integer = null) {
            return [
                $this->method(),

                $this->property ?? [],
                $this->property ?? null,

                $one ?? $two,
                $two ?? $one,
                $one ?? $three,
                (string) ($one ?? $three),

                $string ?? null,

                $integer ?? ( time() + self::DEFAULT_LIFETIME ),
            ];
        }
    }
}