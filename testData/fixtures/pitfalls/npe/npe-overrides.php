<?php

class CasesHolder {
    function returnsNullable(stdClass $parameter): ?stdClass {}
    function returnsObject(stdClass $parameter): stdClass    {}

    function method() {
        $var = $this->returnsNullable(new stdClass());
        $var = $this->returnsNullable(<warning descr="[EA] Null pointer exception may occur here.">$var</warning>);
        $var = $this->returnsObject(<warning descr="[EA] Null pointer exception may occur here.">$var</warning>);
        $var = $this->returnsObject($var);
    }
}