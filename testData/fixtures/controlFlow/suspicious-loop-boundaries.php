<?php

<error descr="Conditions and repeated operations are not complimentary, please check what's going on here.">for</error>
    ($i = 0; $i < 5; --$i) {}
<error descr="Conditions and repeated operations are not complimentary, please check what's going on here.">for</error>
    ($i = 5; $i > 0; ++$i) {}

/* valid cases: going up */
for ($i = 0; 5 >= $i; ++$i) {}
for ($i = 0; $i < 5; ++$i) {}
for ($i = 0; $i < 5; $i++) {}
for ($i = 0; $i <= 5; $i += 1) {}
for ($i = 0; $i <= 5; $i *= 2) {}
for ($i = 0; $i <= 5; $i = $i + 1) {}

/* valid cases: going down */
for ($i = 5; 0 <= $i; --$i) {}
for ($i = 5; $i > 0; --$i) {}
for ($i = 5; $i > 0; $i--) {}
for ($i = 5; $i >= 0; $i -= 1) {}
for ($i = 5; $i >= 0; $i /= 2) {}
for ($i = 5; $i >= 0; $i = $i - 1) {}

/* false-positive: variable limit */
for ($i = 0; $max > $i; ++$i) {}