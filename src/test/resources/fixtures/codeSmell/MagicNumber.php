<?php

// Allowed: integer zero or one are ignored by default.
$shouldNotBeConstantByDefault_NumberZero = 0;
$shouldNotBeConstantByDefault_NumberOne = 1;

// Warn: float zero or one are not ignored.
$shouldBeConstant_FloatZero = <weak_warning descr="Magic number should be replaced by a constant.">0.0</weak_warning>;
$shouldBeConstnat_FloatOne = <weak_warning descr="Magic number should be replaced by a constant.">1.0</weak_warning>;

// Warn: any number, except zero or one, by default.
$shouldBeConstant_AnyNumber = <weak_warning descr="Magic number should be replaced by a constant.">10</weak_warning>;
$shouldBeConstant_AnyFloat = <weak_warning descr="Magic number should be replaced by a constant.">10.0</weak_warning>;
