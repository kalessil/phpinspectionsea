<?php

class GenericProvider {
    /** @return $this */
    public function configure(): self { return $this; }
    /** @return object[] */
    public function fetch() { yield from []; }
}
/** @method stdClass[] fetch() */
class ConcreteProvider extends GenericProvider {
}

function cases_holder() {
    $provider = (new ConcreteProvider())->configure();
    foreach ($provider->fetch() as $object) {}
}