<?php

    class ClassNeedsToStringMethod {}

    /* pattern: object can not be used in string context */
    $object = new ClassNeedsToStringMethod();
    $result = <error descr="Class \ClassNeedsToStringMethod must implement __toString().">$object == '...'</error>;
    $result = <error descr="Class \ClassNeedsToStringMethod must implement __toString().">$object != '...'</error>;
    $result = <error descr="Class \ClassNeedsToStringMethod must implement __toString().">$object <> '...'</error>;

    /* pattern: safe comparison */
    $result = <warning descr="Safely use '===' here.">$x == '...'</warning>;
    $result = <warning descr="Safely use '!==' here.">$x != '...'</warning>;
    $result = <warning descr="Safely use '!==' here.">$x <> '...'</warning>;

    /* pattern: needs hardening */
    $result = <weak_warning descr="Please consider using more strict '===' here (hidden types casting will not be applied anymore).">$x == ''</weak_warning>;
    $result = <weak_warning descr="Please consider using more strict '!==' here (hidden types casting will not be applied anymore).">$x != ''</weak_warning>;
    $result = <weak_warning descr="Please consider using more strict '===' here (hidden types casting will not be applied anymore).">$x == '0'</weak_warning>;
    $result = <weak_warning descr="Please consider using more strict '!==' here (hidden types casting will not be applied anymore).">$x != '0'</weak_warning>;
    $result = <weak_warning descr="Please consider using more strict '===' here (hidden types casting will not be applied anymore).">$x == $y</weak_warning>;
    $result = <weak_warning descr="Please consider using more strict '!==' here (hidden types casting will not be applied anymore).">$x != $y</weak_warning>;