<?php

class PUTTtstAnnotation {
    /** <weak_warning descr="[EA] @test is ambiguous because method name starts with 'test'.">@test</weak_warning> */
    public function testMethod() {}

    /** @test */
    public function method()     {}
}