<?php

/* pattern: exist expressions */
if ($condition) { die; }  <warning descr="'else' is not needed here (because of the last statement in if-branch).">else</warning> { ; }
if ($condition) { exit; } <warning descr="'else' is not needed here (because of the last statement in if-branch).">else</warning> { ; }

/* pattern: return point expressions */
if ($condition) { return; }   <warning descr="'else' is not needed here (because of the last statement in if-branch).">else</warning> { ; }
if ($condition) { throw $e; } <warning descr="'else' is not needed here (because of the last statement in if-branch).">else</warning> { ; }
for (;;) {
    if ($condition) { break; }    <warning descr="'else' is not needed here (because of the last statement in if-branch).">else</warning> { ; }
    if ($condition) { continue; } <warning descr="'else' is not needed here (because of the last statement in if-branch).">else</warning> { ; }
}

/* pattern: alternative branches invariants */
if ($condition) { return; }
    <warning descr="'else' is not needed here (because of the last statement in if-branch).">else</warning> { ; }
if ($condition) { return; }
    <warning descr="'elseif' is not needed here (because of the last statement in if-branch).">elseif</warning> ($condition)  { ; }
if ($condition) { return; }
    <warning descr="'elseif' is not needed here (because of the last statement in if-branch).">elseif</warning> ($condition)  { ; }  elseif($condition) { ; }
if ($condition) { return; }
    <warning descr="'else' is not needed here (because of the last statement in if-branch).">else</warning> if ($condition) { ; }
if ($condition) { return; }
    <warning descr="'else' is not needed here (because of the last statement in if-branch).">else</warning> if ($condition) { ; } else { ; }

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