<?php

namespace {
    class ClazzOne {}
    class ClazzTwo extends ClazzOne{}
    class ClazzThree {}

    define(DEFAULT_LIFETIME,  3600 * 24);

    abstract class CasesHolder {
        const DEFAULT_LIFETIME = 3600 * 24;

        /** @var CasesHolder|null */
        private $property;

        abstract function method();

        public function cases(ClazzOne $one, ClazzTwo $two, ClazzThree $three, string $string = null, int $integer = null) {
            return [
                <weak_warning descr="[EA] It possible to use '$this->method()' instead (reduces cognitive load).">$this->method() ?? null</weak_warning>,

                <weak_warning descr="[EA] Resolved operands types are not complimentary, while they should be ([\CasesHolder] vs [array]).">$this->property ?? []</weak_warning>,
                $this->property ?? null,

                $one ?? $two,
                $two ?? $one,
                <weak_warning descr="[EA] Resolved operands types are not complimentary, while they should be ([\ClazzOne] vs [\ClazzThree]).">$one ?? $three</weak_warning>,
                (string) ($one ?? $three),

                $string ?? null,

                $integer ?? ( time() + self::DEFAULT_LIFETIME ),
                $integer ?? ( time() + DEFAULT_LIFETIME ),
            ];
        }
    }
}