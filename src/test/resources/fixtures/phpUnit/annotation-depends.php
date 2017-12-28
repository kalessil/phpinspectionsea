<?php

class DependsAnnotationHolder {
    abstract function inner();

    /** @depends inner */
    abstract function dependsCorrect();

    /** @depends outer */
    abstract public function <error descr="@depends referencing to a non-existing entity.">dependsTypo</error>();
}