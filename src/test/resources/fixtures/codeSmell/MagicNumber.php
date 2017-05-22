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
return sum(1, 2);

// Warn: using in binary expression.
$binaryExpressionOnLeft  = <weak_warning descr="Magic number should be replaced by a constant.">5</weak_warning> < $number;
$binaryExpressionOnRight = $number > <weak_warning descr="Magic number should be replaced by a constant.">5</weak_warning>;

// False-positive: generalized way to test expression type.
$shouldNotBeTested = [ 'array' => 'value' ];
