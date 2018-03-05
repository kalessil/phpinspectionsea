<?php

namespace OtherNamespace {
    function join() {}

    echo join();
    echo \<warning descr="'join(...)' is an alias function, consider using 'implode(...)' instead.">join</warning>();
}

namespace CasesHolder {

    use function OtherNamespace\join;

    echo join();
    echo \<warning descr="'join(...)' is an alias function, consider using 'implode(...)' instead.">join</warning>();
}