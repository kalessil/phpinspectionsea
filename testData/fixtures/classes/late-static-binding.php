<?php

class Clazz {
    private function privateMethod() {}

    public function publicMethod() {
        <weak_warning descr="It's better to use 'self' here (identically named private method in child classes will cause an error).">static</weak_warning>::privateMethod();

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