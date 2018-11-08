<?php

class Clazz {
    private function privateMethod() {}

    public function publicMethod() {
        self::privateMethod();

        self::privateMethod();
        $this->privateMethod();
    }
}

final class FinalClazz {
    private function privateMethod() {}

    public function publicMethod() {
        static::privateMethod();
    }
}