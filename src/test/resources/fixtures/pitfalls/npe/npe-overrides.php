<?php

class CasesHolder {
    function canBeNull(stdClass $parameter): ?stdClass    {}
    function canNotBeNull(stdClass $parameter): stdClass  {}

    function cases_holder() {
        $var = $this->canBeNull(new stdClass());
        $var = $this->canBeNull(<warning descr="Null pointer exception may occur here.">$var</warning>);
        $var = $this->canNotBeNull(<warning descr="Null pointer exception may occur here.">$var</warning>);
        $var = $this->canNotBeNull($var);
    }
}