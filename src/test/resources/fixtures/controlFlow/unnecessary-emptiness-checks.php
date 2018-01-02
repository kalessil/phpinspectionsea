<?php

class CasesHolder {
    private function simplification() {
        return [
            <warning descr="'empty(...) && ... === null' here can be replaced with '!isset(...)'.">empty($x)</warning> && $x === null,
            empty($x) && $x !== null,
            !<warning descr="!empty(...) || ... !== null' here can be replaced with 'isset(...)'.">empty($x)</warning> || $x !== null,
            !empty($x) || $x === null,
            <warning descr="'isset(...) && ...' here can be replaced with '!empty(...)'.">isset($x)</warning> && $x,
            isset($x) && !$x,
            !<warning descr="'!isset(...) || !...' here can be replaced with 'empty(...)'.">isset($x)</warning> || !$x,
            !isset($x) || $x,
        ];
    }

    private function processAll()
    {
        return [
            isset($x)
                && !<warning descr="Doesn't match to previous null value handling (perhaps always false when reached).">isset($x)</warning>
                && <warning descr="Seems to be always true when reached.">$x !== null</warning>,
            empty($x)
                && !<warning descr="Doesn't match to previous falsy value handling (perhaps always false when reached).">empty($x)</warning>
                && !<warning descr="Seems to be always true when reached.">$x</warning>,
        ];
    }
    //  <warning descr=""></warning>
}