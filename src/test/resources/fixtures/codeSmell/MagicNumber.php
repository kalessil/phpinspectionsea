<?php

// Allowed: should not affects pure assignments.
$shouldNotBeConstant_ItsJustAnAssignment = 10;

// Warn: any returned integer or float.
return <weak_warning descr="Magic number should be replaced by a constant.">0</weak_warning>;
return <weak_warning descr="Magic number should be replaced by a constant.">1</weak_warning>;
return <weak_warning descr="Magic number should be replaced by a constant.">0.0</weak_warning>;
return <weak_warning descr="Magic number should be replaced by a constant.">1.0</weak_warning>;
return <weak_warning descr="Magic number should be replaced by a constant.">10</weak_warning>;
return <weak_warning descr="Magic number should be replaced by a constant.">10.0</weak_warning>;

// Allowed: ignore indirect returns.
return $pregMatch[2];

// Warn: using in binary expression.
$binaryExpressionOnLeft  = <weak_warning descr="Magic number should be replaced by a constant.">5</weak_warning> < $number;
$binaryExpressionOnRight = $number > <weak_warning descr="Magic number should be replaced by a constant.">5</weak_warning>;

// False-positive: is not a number.
$shouldNotBeTested    = [ 'array' => 'value' ];

// False-positive: typehinted is not a numeric literal.
class TypehintCheck {
    private $isNotANumericLiteral = 0;
    public function propertyIsNotANumericLiteral() { $this->isNotANumericLiteral !== null; }
    public function parameterIsNotANumericLiteral(int $number) { $number !== null; }
}

// Allowed: zero or one is allowed in binary expression if is used for counting.
count($values) >  0;
count($values) >  1;
count($values) >= 0;
count($values) >= 1;
$this->size() >  0;
$this->size() >  1;
$this->size() >= 0;
$this->size() >= 1;
$count >  0;
$count >  1;
$count >= 0;
$count >= 1;
0 <  count($values);
1 <  count($values);
0 <= count($values);
1 <= count($values);
0 <  $this->size();
1 <  $this->size();
0 <= $this->size();
1 <= $this->size();
0 <  $count;
1 <  $count;
0 <= $count;
1 <= $count;

// Warn: if zero or one is not used for counting.
count($values) <  <weak_warning descr="Magic number should be replaced by a constant.">0</weak_warning>;
count($values) <  <weak_warning descr="Magic number should be replaced by a constant.">1</weak_warning>;
count($values) <= <weak_warning descr="Magic number should be replaced by a constant.">0</weak_warning>;
count($values) <= <weak_warning descr="Magic number should be replaced by a constant.">1</weak_warning>;
$this->size() <  <weak_warning descr="Magic number should be replaced by a constant.">0</weak_warning>;
$this->size() <  <weak_warning descr="Magic number should be replaced by a constant.">1</weak_warning>;
$this->size() <= <weak_warning descr="Magic number should be replaced by a constant.">0</weak_warning>;
$this->size() <= <weak_warning descr="Magic number should be replaced by a constant.">1</weak_warning>;
$count <  <weak_warning descr="Magic number should be replaced by a constant.">0</weak_warning>;
$count <  <weak_warning descr="Magic number should be replaced by a constant.">1</weak_warning>;
$count <= <weak_warning descr="Magic number should be replaced by a constant.">0</weak_warning>;
$count <= <weak_warning descr="Magic number should be replaced by a constant.">1</weak_warning>;
<weak_warning descr="Magic number should be replaced by a constant.">0</weak_warning> >  count($values);
<weak_warning descr="Magic number should be replaced by a constant.">1</weak_warning> >  count($values);
<weak_warning descr="Magic number should be replaced by a constant.">0</weak_warning> >= count($values);
<weak_warning descr="Magic number should be replaced by a constant.">1</weak_warning> >= count($values);
<weak_warning descr="Magic number should be replaced by a constant.">0</weak_warning> >  $this->size();
<weak_warning descr="Magic number should be replaced by a constant.">1</weak_warning> >  $this->size();
<weak_warning descr="Magic number should be replaced by a constant.">0</weak_warning> >= $this->size();
<weak_warning descr="Magic number should be replaced by a constant.">1</weak_warning> >= $this->size();
<weak_warning descr="Magic number should be replaced by a constant.">0</weak_warning> >  $count;
<weak_warning descr="Magic number should be replaced by a constant.">1</weak_warning> >  $count;
<weak_warning descr="Magic number should be replaced by a constant.">0</weak_warning> >= $count;
<weak_warning descr="Magic number should be replaced by a constant.">1</weak_warning> >= $count;

// Allowed: zero is allowed in binary expression if used for counting zero elements.
count($values) === 0;
count($values) !== 0;
$this->size() === 0;
$this->size() !== 0;
$count === 0;
$count !== 0;
0 === count($values);
0 !== count($values);
0 === $this->size();
0 !== $this->size();
0 === $count;
0 !== $count;

// Warn: it is not applicable to other values.
count($values) >= <weak_warning descr="Magic number should be replaced by a constant.">2</weak_warning>;
$this->size() >= <weak_warning descr="Magic number should be replaced by a constant.">2</weak_warning>;
$count >= <weak_warning descr="Magic number should be replaced by a constant.">2</weak_warning>;
<weak_warning descr="Magic number should be replaced by a constant.">2</weak_warning> <= count($values);
<weak_warning descr="Magic number should be replaced by a constant.">2</weak_warning> <= $this->size();
<weak_warning descr="Magic number should be replaced by a constant.">2</weak_warning> <= $count;
<weak_warning descr="Magic number should be replaced by a constant.">1</weak_warning> === count($values);
<weak_warning descr="Magic number should be replaced by a constant.">1</weak_warning> !== count($values);
<weak_warning descr="Magic number should be replaced by a constant.">1</weak_warning> === $this->size();
<weak_warning descr="Magic number should be replaced by a constant.">1</weak_warning> !== $this->size();
<weak_warning descr="Magic number should be replaced by a constant.">1</weak_warning> === $count;
<weak_warning descr="Magic number should be replaced by a constant.">1</weak_warning> !== $count;

// Warn: if used on switch.
switch (getSomeNumber()) {
    case <weak_warning descr="Magic number should be replaced by a constant.">0</weak_warning>: $anyNumberEvenZero = true; break;
    case <weak_warning descr="Magic number should be replaced by a constant.">5</weak_warning>: $anyOtherNumber    = true; break;
    default: $shouldBeJustIgnored = true; break;
}

// Warn: negative values should be affected too.
$negativeValue >= <weak_warning descr="Magic number should be replaced by a constant.">-10</weak_warning>;
$negativeValue >= <weak_warning descr="Magic number should be replaced by a constant.">-1</weak_warning>;
$negativeValue >= <weak_warning descr="Magic number should be replaced by a constant.">-0</weak_warning>;

class PropertyNumericClass {
    // Allowed: const should not be checked.
    const IGNORE_THAT = 10;

    // Warn: properties should not be numberic too.
    public $someNumbericValue = <weak_warning descr="Magic number should be replaced by a constant.">10</weak_warning>;

    // Allowed: except for zero.
    public $zeroValue  = 0;
    public $ignoreThat = self::IGNORE_THAT;
}

// Warn: number used in multiply expression.
$expressionWithMultiply * <weak_warning descr="Magic number should be replaced by a constant.">10</weak_warning>;
$expressionWithMultiply * <weak_warning descr="Magic number should be replaced by a constant.">-10</weak_warning>;
$expressionWithMultiply *= <weak_warning descr="Magic number should be replaced by a constant.">10</weak_warning>;
$expressionWithMultiply *= <weak_warning descr="Magic number should be replaced by a constant.">-10</weak_warning>;

// Allowed: number -1 used in multiply.
$expressionWithMultiply * -1;
$expressionWithMultiply *= -1;

// Warn: used as default value for parameters.
function shouldNotBeAllowedOnParameter($shouldNotBeAllowedOnParameter = <weak_warning descr="Magic number should be replaced by a constant.">10</weak_warning>) { }

// Allowed: except by zero.
function shouldAcceptOnlyZeroOnParameter($shouldAcceptOnlyZeroOnParameter = 0) { }

// Warn: used as argument.
writeNumber(<weak_warning descr="Magic number should be replaced by a constant.">0</weak_warning>);
writeNumber(<weak_warning descr="Magic number should be replaced by a constant.">10</weak_warning>);
$this->writeNumber(<weak_warning descr="Magic number should be replaced by a constant.">10</weak_warning>);

// Allowed: except if it matches with the default value (even for long constant references).
const NUMBER_TEN = 10;
const NUMBER_TEN_REFERENCE = NUMBER_TEN;
function sameAsDefaultValue($defaultValue = NUMBER_TEN_REFERENCE) { }
sameAsDefaultValue(10);

// Warn: using zero as argument to a not int parameter.
function mixedParameterFunction($mixed) {}

mixedParameterFunction(<weak_warning descr="Magic number should be replaced by a constant.">0</weak_warning>);

// Allowed: using zero as argument to int parameter.
function intParameterFunction(int $int) {}
function nullableIntParameterFunction(?int $int) {}

intParameterFunction(0);
nullableIntParameterFunction(0);
