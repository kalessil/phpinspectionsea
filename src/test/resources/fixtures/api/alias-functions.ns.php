<?php

namespace OtherNamespace {
    function join() {}
}

namespace CasesHolder {
    use function OtherNamespace\join;

    echo join();
    echo <warning descr="'sizeof(...)' is an alias function. Use 'count(...)' instead.">sizeof()</warning>;
}