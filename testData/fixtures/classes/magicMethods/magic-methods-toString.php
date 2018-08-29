<?php

class ClassToStringStatic {
    static public function <error descr="__toString cannot be static.">__toString</error>() {
        return '';
    }
}

class ClassToStringWithArguments {
    public function <error descr="__toString cannot accept arguments.">__toString</error>($argument) {
        return '';
    }
}

class ClassToStringNotPublic {
    protected function <error descr="__toString must be public.">__toString</error>() {
        return '';
    }
}

class ClassToStringReturnTypes {
    private $property;

    public function __toString() {
        if ('' === '-') {
            /* false-positives: type is not resolved at all */
            return $this->property;
        }
        <error descr="__toString must return string.">return [];</error>
    }
}