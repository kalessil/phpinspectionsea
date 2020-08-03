<?php

class ClassToStringStatic {
    static public function <error descr="[EA] __toString cannot be static.">__toString</error>() {
        return '';
    }
}

class ClassToStringWithArguments {
    public function <error descr="[EA] __toString cannot accept arguments.">__toString</error>($argument) {
        return '';
    }
}

class ClassToStringNotPublic {
    protected function <error descr="[EA] __toString must be public.">__toString</error>() {
        return '';
    }
}

class ClassToStringReturnedTypes {
    private $property;

    public function __toString() {
        if ('' === '-') {
            /* false-positives: type is not resolved at all */
            return $this->property;
        }
        <error descr="[EA] __toString must return string.">return [];</error>
    }
}

class ClassToStringImplicitReturnTypes {
    public function <error descr="[EA] __toString must return string.">__toString</error>(): array {
        return [];
    }
}