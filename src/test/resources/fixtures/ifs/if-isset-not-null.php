<?php

/* multiple isset arguments pattern */
if (
    isset($obj1, $obj2) &&
    <weak_warning descr="It seems like 'null !== $obj1' is already covered by isset">null !== $obj1</weak_warning> &&
    <weak_warning descr="It seems like '$obj2 !== null' is already covered by isset">$obj2 !== null</weak_warning>
) {
    echo 'Object';
}

/* both inverted and regular patterns */
if (isset($obj) && <weak_warning descr="It seems like 'null !== $obj' is already covered by isset">null !== $obj</weak_warning>) {
    echo 'Object';
}
if (!isset($obj) && <weak_warning descr="It seems like 'null !== $obj' is already covered by isset">null !== $obj</weak_warning>) {
    echo 'Object';
}

/* logical or operands should also be handled */
if (isset($obj) || <weak_warning descr="It seems like '$obj !== null' is already covered by isset">$obj !== null</weak_warning>) {
    echo 'Object';
}