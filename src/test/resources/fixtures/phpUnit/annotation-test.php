<?php

class PUTTtstAnnotation {
    /** <warning descr="@test is ambiguous because method name starts with 'test'.">@test</warning> */
    public function testMethod() {}

    /** @test */
    public function method()     {}
}