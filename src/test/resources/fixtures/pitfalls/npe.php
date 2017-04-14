<?php

class InvokableClass extends \stdClass {
    public function __invoke($x) {}
}

class CasesHolder {
    /** @var \InvokableClass $fourth */
    public function npeReportingBasic(
        ?\InvokableClass $first,
        ?\InvokableClass $second = null,
         \InvokableClass $third = null,
         $fourth = null
    ) {
        $fourth->property = 'NPE is not reported - we do not rely onto DocBlock';

        /* case 1: multiple reports, until nullability checked */
        <warning descr="Null pointer exception may occur here.">$first</warning>->property = 'NPE reported';
        <warning descr="Null pointer exception may occur here.">$first</warning>();
        if (null !== $first) {
            return $first->property;
        }

        <warning descr="Null pointer exception may occur here.">$second</warning>->property = 'NPE reported';
        if ($second instanceof \InvokableClass) {
            return $second->property;
        }

        <warning descr="Null pointer exception may occur here.">$third</warning>->property = 'NPE reported';
        if ($third) {
            return $third->property;
        }
    }

    public function npeReportingBasicExtended(
        \InvokableClass $first = null,
        \InvokableClass $second = null,
        \InvokableClass $third
    ) {
        $third->property = 'NPE is not reported - the parameter is not nullable';

        <warning descr="Null pointer exception may occur here.">$first</warning>->property = 'NPE reported';
        if (isset($first)) {
            return $first->property;
        }

        <warning descr="Null pointer exception may occur here.">$second</warning>->property = 'NPE reported';
        if (!empty($second)) {
            return $second->property;
        }
    }

    public function npeCheckConstraints(\InvokableClass $first = null) {
        $first = $first ?: null;
        $first->property = 'Obviously NPE, but we stop at re-assigning. No solution as of April 2017.';
    }
}