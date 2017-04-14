<?php

class CasesHolder {
    /** @var \stdClass $fourth */
    public function npeReportingBasic(
        ?\stdClass $first,
        ?\stdClass $second = null,
         \stdClass $third = null,
         $fourth = null
    ) {
        $fourth->property = 'NPE is not reported - we do not rely onto DocBlock';

        /* case 1: multiple reports, until nullability checked */
        <warning descr="Null pointer exception may occur here.">$first</warning>->property = 'NPE reported';
        <warning descr="Null pointer exception may occur here.">$first</warning>->property = 'NPE reported';
        if (null !== $first) {
            return $first->property;
        }

        <warning descr="Null pointer exception may occur here.">$second</warning>->property = 'NPE reported';
        if ($second instanceof \stdClass) {
            return $second->property;
        }

        <warning descr="Null pointer exception may occur here.">$third</warning>->property = 'NPE reported';
        if ($third) {
            return $third->property;
        }
    }

    public function npeReportingBasicExtended(
        \stdClass $first = null,
        \stdClass $second = null,
        \stdClass $third
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
}