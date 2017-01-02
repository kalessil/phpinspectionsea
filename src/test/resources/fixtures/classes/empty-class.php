<?php

class <weak_warning descr="Class does not contain any properties or methods.">AnEmptyClass</weak_warning> {
}

trait ATrait {
}
class AClassWithATrait {
    use ATrait;
}
class <weak_warning descr="Class does not contain any properties or methods.">AnEmptyClassWithTransitiveTrait</weak_warning>
    extends AClassWithATrait {
}