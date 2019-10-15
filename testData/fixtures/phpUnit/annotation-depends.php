<?php

class DependsAnnotationHolder {
    /** @test */
    abstract function inner();

    /** @depends inner */
    abstract function dependsCorrect();

    /** @depends outer */
    abstract public function <error descr="[EA] @depends referencing to a non-existing or inappropriate entity.">dependsWrongReference</error>();

    /** @depends \DependsAnnotationHolder::outer */
    abstract public function <error descr="[EA] @depends referencing to a non-existing or inappropriate entity.">dependsWrongFqnReference</error>();

    abstract function dataProvider();

    /** @depends dataProvider */
    abstract public function <error descr="[EA] @depends referencing to a non-existing or inappropriate entity.">dependsInappropriate</error>();
}