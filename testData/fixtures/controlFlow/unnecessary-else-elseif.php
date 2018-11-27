<?php

/* pattern: exist expressions */
if ($condition) { die; }  <warning descr="Child instructions can be extracted here (clearer intention, lower complexity numbers).">else</warning> { ; }

if ($condition) { exit; } <warning descr="Child instructions can be extracted here (clearer intention, lower complexity numbers).">else</warning> { ; }

/* pattern: return point expressions */
if ($condition) { return; }   <warning descr="Child instructions can be extracted here (clearer intention, lower complexity numbers).">else</warning> { ; }

if ($condition) { throw $e; } <warning descr="Child instructions can be extracted here (clearer intention, lower complexity numbers).">else</warning> { ; }

for (;;) {
    if ($condition) { break; }    <warning descr="Child instructions can be extracted here (clearer intention, lower complexity numbers).">else</warning> { ; }

    if ($condition) { continue; } <warning descr="Child instructions can be extracted here (clearer intention, lower complexity numbers).">else</warning> { ; }
}

/* pattern: alternative branches invariants */
if ($condition) { return; }
    <warning descr="Child instructions can be extracted here (clearer intention, lower complexity numbers).">else</warning> { ; }

if ($condition) { return; }
    <warning descr="Can be converted into if-branch (clearer intention, lower complexity numbers).">elseif</warning> ($condition)  { ; }

if ($condition) { return; }
    <warning descr="Can be converted into if-branch (clearer intention, lower complexity numbers).">elseif</warning> ($condition)  { ; }  elseif($condition) { ; }

if ($condition) { return; }
    <warning descr="Child instructions can be extracted here (clearer intention, lower complexity numbers).">else</warning> if ($condition) { ; }

if ($condition) { return; }
    <warning descr="Child instructions can be extracted here (clearer intention, lower complexity numbers).">else</warning> if ($condition) { ; } else { ; }

/* false-positives: alternative syntax */
if ($condition):
    return;
else:
    ;
endif;

/* false-positives: else-context */
if ($condition) { ; } else if ($condition) { return; } else { ; }

/* false-positives: structure not as expected */
if ($condition) { ; }       else { ; }
if ($condition) {}          else {}
if ($condition) return;     else ;
if ($condition) ;           else return;
if ($condition) { return; } else ;
if ($condition) { return; } elseif ($condition) ;