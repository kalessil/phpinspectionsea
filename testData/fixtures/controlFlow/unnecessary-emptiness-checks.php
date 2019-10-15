<?php

class CasesHolder {
    private function simplification() {
        return [
            <warning descr="[EA] 'empty(...) && ... === null' here can be replaced with '!isset($x)' (simplification).">empty($x)</warning> && $x === null,
            empty($x) && $x !== null,
            !<warning descr="[EA] '!empty(...) || ... !== null' here can be replaced with 'isset($x)' (simplification).">empty($x)</warning> || $x !== null,
            !empty($x) || $x === null,
            <warning descr="[EA] 'isset(...) && ...' here can be replaced with '!empty($x)' (simplification).">isset($x)</warning> && $x,
            isset($x) && !$x,
            !<warning descr="[EA] '!isset(...) || !...' here can be replaced with 'empty($x)' (simplification).">isset($x)</warning> || !$x,
            !isset($x) || $x,
        ];
    }

    private function processAll() {
        return [
            isset($x)
                && !<warning descr="[EA] Doesn't match to previous null value handling (perhaps always false when reached).">isset($x)</warning>
                && <warning descr="[EA] Seems to be always true when reached.">$x !== null</warning>,
            empty($x)
                && !<warning descr="[EA] Doesn't match to previous falsy value handling (perhaps always false when reached).">empty($x)</warning>
                && !<warning descr="[EA] Seems to be always true when reached.">$x</warning>,
        ];
    }

    private function issetEmptyMix() {
        return[
            <warning descr="[EA] Perhaps can be dropped, as it covered by a following 'empty(...)'.">isset($x)</warning> && !empty($x),
            isset($x) && <warning descr="[EA] Doesn't match to previous isset-alike handling (perhaps always false when reached).">empty($x)</warning>,
            isset($x) || !<warning descr="[EA] Seems to be always false when reached.">empty($x)</warning>,
            isset($x) || <warning descr="[EA] Doesn't match to previous isset-alike handling (perhaps always false when reached).">empty($x)</warning>,
            !<warning descr="[EA] Perhaps can be dropped, as it covered by a following 'empty(...)'.">isset($x)</warning> || empty($x),
            !isset($x) || !<warning descr="[EA] Doesn't match to previous isset-alike handling (perhaps always false when reached).">empty($x)</warning>,
            !isset($x) && <warning descr="[EA] Seems to be always true when reached.">empty($x)</warning>,
            !isset($x) && !<warning descr="[EA] Doesn't match to previous isset-alike handling (perhaps always false when reached).">empty($x)</warning>,
        ];
    }

    private function nullValueChecks() {
        return [
            isset($x) && <warning descr="[EA] Seems to be always true when reached.">$x !== null</warning>,
            isset($x) || <warning descr="[EA] Seems to be always true when reached.">$x !== null</warning>,
            !isset($x) || <warning descr="[EA] Seems to be always true when reached.">$x === null</warning>,
            !isset($x) && <warning descr="[EA] Seems to be always true when reached.">$x === null</warning>,
            isset($x) && <warning descr="[EA] Doesn't match to previous null value handling (perhaps always false when reached).">$x === null</warning>,
            isset($x) || <warning descr="[EA] Doesn't match to previous null value handling (perhaps always false when reached).">$x === null</warning>,
            !isset($x) || <warning descr="[EA] Doesn't match to previous null value handling (perhaps always false when reached).">$x !== null</warning>,
            !isset($x) && <warning descr="[EA] Doesn't match to previous null value handling (perhaps always false when reached).">$x !== null</warning>,
        ];
    }

    private function falsyValueChecks() {
        return [
            empty($x) && !<warning descr="[EA] Seems to be always true when reached.">$x</warning>,
            !empty($x) || <warning descr="[EA] Seems to be always true when reached.">$x</warning>,
            empty($x) && <warning descr="[EA] Doesn't match to previous falsy value handling (perhaps always false when reached).">$x</warning>,
            !empty($x) || !<warning descr="[EA] Doesn't match to previous falsy value handling (perhaps always false when reached).">$x</warning>,
        ];
    }

    private function reportingTargetsSelection() {
        return [
            isset(<warning descr="[EA] 'isset(...) && ...' here can be replaced with '!empty($x)' (simplification).">$x</warning>, $y) && $x,
            !isset(<warning descr="[EA] '!isset(...) || !...' here can be replaced with 'empty($x)' (simplification).">$x</warning>, $y) || !$x,
        ];
    }

    private function issetInNullCoallescingContext($parameter) {
        return [
            <weak_warning descr="[EA] '$parameter ?? '...'' can be used instead (simplification, reduces cognitive load).">isset($parameter) ?? '...'</weak_warning>,
            <weak_warning descr="[EA] '$parameter ?? '...'' can be used instead (simplification, reduces cognitive load).">isset($parameter) ?: '...'</weak_warning>,
        ];
    }

    private function emptyArrayChecks($parameter, $more) {
        return [
            is_array($parameter) && empty($parameter),
            is_array($parameter) && !empty($parameter),

            is_array($parameter) && $parameter,
            is_array($parameter) && !$parameter,

            is_array($parameter) && <warning descr="[EA] 'is_array(...) && count(...)' here probably can be replaced with '$parameter && is_array($parameter)' (simplification).">count($parameter)</warning>,
            is_array($parameter) && !<warning descr="[EA] 'is_array(...) && !count(...)' here probably can be replaced with '!$parameter && is_array($parameter)' (simplification).">count($parameter)</warning>,

            is_array($parameter) && <warning descr="[EA] 'is_array(...) && count(...)' here probably can be replaced with '$parameter && is_array($parameter)' (simplification).">count($parameter) > 0</warning>,
            is_array($parameter) && <warning descr="[EA] 'is_array(...) && count(...)' here probably can be replaced with '$parameter && is_array($parameter)' (simplification).">count($parameter) != 0</warning>,
            is_array($parameter) && <warning descr="[EA] 'is_array(...) && count(...)' here probably can be replaced with '$parameter && is_array($parameter)' (simplification).">count($parameter) !== 0</warning>,
            is_array($parameter) && <warning descr="[EA] 'is_array(...) && !count(...)' here probably can be replaced with '!$parameter && is_array($parameter)' (simplification).">count($parameter) == 0</warning>,
            is_array($parameter) && <warning descr="[EA] 'is_array(...) && !count(...)' here probably can be replaced with '!$parameter && is_array($parameter)' (simplification).">count($parameter) === 0</warning>,

            is_array($parameter) && empty($more),
            is_array($parameter) && !empty($more),
        ];
    }

    private function callsHandlingCorrectness($parameter) {
        return [
            empty($parameter) || strlen(trim($parameter)) > 0,
            isset($parameter) || strlen(trim($parameter)) > 0,
            $parameter || strlen(trim($parameter)) > 0,

            empty($parameter) || strlen($parameter) > 0,
            isset($parameter) || strlen($parameter) > 0,
            $parameter || strlen($parameter) > 0,

            empty($parameter) || strlen($parameter),
            isset($parameter) || strlen($parameter),
            $parameter || strlen($parameter),

            empty($parameter) || !strlen($parameter),
            isset($parameter) || !strlen($parameter),
            $parameter || !strlen($parameter),
        ];
    }
}