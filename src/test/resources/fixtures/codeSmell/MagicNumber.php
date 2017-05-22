<?php

// Allowed: should not affects pure assignments.
$shouldNotBeConstant_ItsJustAnAssignment = 10;

// Allowed: integer zero or one are ignored by default.
return 0;
return 1;

// Warn: float zero or one are not ignored.
return <weak_warning descr="Magic number should be replaced by a constant.">0.0</weak_warning>;
return <weak_warning descr="Magic number should be replaced by a constant.">1.0</weak_warning>;

// Warn: any number, except zero or one, by default.
return <weak_warning descr="Magic number should be replaced by a constant.">10</weak_warning>;
return <weak_warning descr="Magic number should be replaced by a constant.">10.0</weak_warning>;

// Allowed: ignore indirect returns.
return $pregMatch[2];
return sum(1, 2);

// False-positive: generalized way to test expression type.
$shouldNotBeTested = [ 'array' => 'value' ];
