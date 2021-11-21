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
        <warning descr="[EA] Null pointer exception may occur here.">$first</warning>->property = '...';
        <warning descr="[EA] Null pointer exception may occur here.">$first</warning>();
        if (null !== $first) {
            return $first->property;
        }

        <warning descr="[EA] Null pointer exception may occur here.">$second</warning>->property = '...';
        if ($second instanceof \InvokableClass) {
            return $second->property;
        }

        <warning descr="[EA] Null pointer exception may occur here.">$third</warning>->property = '...';
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

        <warning descr="[EA] Null pointer exception may occur here.">$first</warning>->property = '...';
        if (isset($first)) {
            return $first->property;
        }

        <warning descr="[EA] Null pointer exception may occur here.">$second</warning>->property = '...';
        if (!empty($second)) {
            return $second->property;
        }
    }

    public function npeCheckArrayAccess(\stdClass $first = null) {
        return [
            <warning descr="[EA] Null pointer exception may occur here.">$first</warning>['whatever'],
            $whatever[$first]
        ];
    }

    public function npeCheckConstraints(\InvokableClass $first = null, \InvokableClass $second = null) {
        $first = $first ?: null;
        <warning descr="[EA] Null pointer exception may occur here.">$first</warning>->property = '...';

        $second = $second ?? null;
        <warning descr="[EA] Null pointer exception may occur here.">$second</warning>->property = '...';
    }

    public function npeCheckClone(?\stdClass $one) {
        return clone <warning descr="[EA] Null pointer exception may occur here.">$one</warning>;
    }

    public function npeCheckLocalVariables(?\stdClass $one) {
        $local = $one;
        if (!isset($local->property[0])) {
            <warning descr="[EA] Null pointer exception may occur here.">$local</warning>->property = '...';
        }

        $item = $this->npeReportingChainedCalls(function (\stdClass $item): ?\stdClass {
            return $item->property;
        });
    }

    public function npeReportingFunctionReference(?\stdClass $one, \stdClass $two = null, \stdClass $three, $four) {
        return $this->npeReportingFunctionReference(
            $one,
            $one,
            <warning descr="[EA] Null pointer exception may occur here.">$one</warning>,
            $one,
            $one /* an extra parameter to ensure inspection is not crashes */
        );
    }

    public function npeReportingChainedCalls(): ?CasesHolder {
        $x = (new CasesHolder())->npeReportingChainedCalls();
        <warning descr="[EA] Null pointer exception may occur here.">$x</warning>->npeReportingChainedCalls();

        $y = (new CasesHolder())->npeReportingChainedCalls()
            <warning descr="[EA] Null pointer exception may occur here.">-></warning>npeReportingChainedCalls();

        return ($z = $this)->npeReportingChainedCalls()
            <warning descr="[EA] Null pointer exception may occur here.">-></warning>npeReportingChainedCalls();
    }
    public function npeSafeChainedCalls1(): ?CasesHolder {
        if ($this->npeReportingChainedCalls() !== null) { return $this->npeReportingChainedCalls()->npeReportingChainedCalls(); }
        return $this->npeReportingChainedCalls() !== null ? $this->npeReportingChainedCalls()->npeReportingChainedCalls() : null;
    }
    public function npeSafeChainedCalls2(): ?CasesHolder {
        if ($this->npeReportingChainedCalls()) { return $this->npeReportingChainedCalls()->npeReportingChainedCalls(); }
        return $this->npeReportingChainedCalls() ? $this->npeReportingChainedCalls()->npeReportingChainedCalls() : null;
    }
    public function npeSafeChainedCalls3(): ?CasesHolder {
        if ($this->npeReportingChainedCalls() instanceof self) { return $this->npeReportingChainedCalls()->npeReportingChainedCalls(); }
        return $this->npeReportingChainedCalls() instanceof self ? $this->npeReportingChainedCalls()->npeReportingChainedCalls() : null;
    }
    public function npeSafeChainedCalls4(): bool {
        return $this->npeReportingChainedCalls() && $this->npeReportingChainedCalls()->npeReportingChainedCalls();
    }

    public function npeSafeTestCase(?CasesHolder $first, ?CasesHolder $second, ?CasesHolder $third) {
        self::assertNotNull($first);
        Assertion::notNull($second);
        Assert::that($third)->notNull();

        return [
            $first->nonExistingField,
            $second->nonExistingField,
            $third->nonExistingField,
        ];
    }

    public function npeSafeCatchCase() {
        try {
            $exception = null;
        } catch (\Exception $exception) {
            return $exception->getMessage();
        }
    }

    public function npeSafeNullCoalescingCase(?CasesHolder $first) {
        return ($first ?? new CaseHolder())->nonExistingField;
    }

    public function npeSafeTernaryCase(?CasesHolder $first, ?CasesHolder $second) {
        return [
            $first ? $first->nonExistingField : '...',
            ($second) ? $second->nonExistingField : '...',
        ];
    }

    public function npeSafeIsNullFunctionCase(?CasesHolder $first) {
        if (!is_null($first)) {
            return $first->nonExistingField;
        }
    }

    public function npeSafeChecks(?CasesHolder $first, ?CasesHolder $second) {
        if ($first) {
            return $first->nonExistingField;
        } elseif($first && $second) {
            return $second->nonExistingField;
        }
    }

    public function npeSafeNullOnlyVariable() {
        $variable = null;
        $variable['...'] = '...';
    }
}

function cases_holder(?CasesHolder $object) {
    <warning descr="[EA] Null pointer exception may occur here.">$object</warning>->npeCheckLocalVariables();
}

/* @var stdClass[] $array */
function skip_before_assignment_case_holder(array $array) {
    foreach($array as $item) {
       $item->property = '...';
    }
    $item = null;
}

function skip_properties_in_isset_context(CasesHolder $parameter = null) {
    return [
        isset(<warning descr="[EA] Null pointer exception may occur here.">$parameter</warning>->property->method()->property) ? '...' : '...',
        <warning descr="[EA] Null pointer exception may occur here.">$parameter</warning>->property->method()->property ?? '...',

        isset($parameter->property) ? '...' : '...',
        $parameter->property ?? '...',
    ];
}

abstract class WhileSelfAssignmentCase
{
    /** @var WhileSelfAssignmentCase|null */
    private $parent;

    abstract function parent(): ?WhileSelfAssignmentCase;

    public function method(): array
    {
        $local = $this->parent;
        while ($local !== null) {
            $local = $local->parent();
        }
    }
}