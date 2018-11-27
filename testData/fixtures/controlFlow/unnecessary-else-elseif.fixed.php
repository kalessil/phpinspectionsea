<?php

/* pattern: exist expressions */
if ($condition) { die; };

if ($condition) { exit; };

/* pattern: return point expressions */
if ($condition) { return; };

if ($condition) { throw $e; };

for (;;) {
    if ($condition) { break; };

    if ($condition) { continue; };
}

/* pattern: alternative branches invariants */
if ($condition) { return; };

if ($condition) { return; }

if ($condition) { ; }

if ($condition) { return; }

if ($condition) { ; } elseif($condition) { ; }

if ($condition) { return; }

if ($condition) { ; }

if ($condition) { return; }

if ($condition) { ; } else { ; }

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