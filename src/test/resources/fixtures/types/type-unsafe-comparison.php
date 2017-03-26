<?php

    class ClassNeedsToStringMethod {}

    /* pattern: object can not be used in string context */
    $object = new ClassNeedsToStringMethod();
    $result = <error descr="Class \ClassNeedsToStringMethod must implement __toString().">$object == '...'</error>;
    $result = <error descr="Class \ClassNeedsToStringMethod must implement __toString().">$object != '...'</error>;
    $result = <error descr="Class \ClassNeedsToStringMethod must implement __toString().">$object <> '...'</error>;

    /* pattern: safe comparison */
    $result = <warning descr="Safely use '===' here.">$x == ''</warning>;
    $result = <warning descr="Safely use '!==' here.">$x != ''</warning>;
    $result = <warning descr="Safely use '!==' here.">$x <> ''</warning>;
    $result = <warning descr="Safely use '===' here.">$x == '...'</warning>;
    $result = <warning descr="Safely use '!==' here.">$x != '...'</warning>;

    /* pattern: needs hardening */
    $result = <weak_warning descr="Hardening to type safe '===', '!==' will cover/point to types casting issues.">$x == '0'</weak_warning>;
    $result = <weak_warning descr="Hardening to type safe '===', '!==' will cover/point to types casting issues.">$x != '0'</weak_warning>;