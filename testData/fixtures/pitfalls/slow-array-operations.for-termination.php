<?php

// Allowed.
for ($i = 0, $iMax = count($values); $i < $iMax; $i++) {}
for ($i = 0; ; $i++) {}

// Warning (case #1).
for ($i = 0; <error descr="[EA] 'count(...)' is used in a loop and is a low performing construction.">$i < count($values)</error>; $i++) {}
for ($i->loop['index'] = 0; <error descr="[EA] 'count(...)' is used in a loop and is a low performing construction.">$i->loop['index'] < count($values)</error>; $i->loop['index']++) {}
for ($i = 0; <error descr="[EA] 'count(...)' is used in a loop and is a low performing construction.">count($values) >= $i</error>; $i++) {}
for ($i = 0, $j = 0; <error descr="[EA] 'count(...)' is used in a loop and is a low performing construction.">count($values) >= $i</error>; $i++) {}

// Warning (case #2).
for ($i = 0; <error descr="[EA] 'count(...)' is used in a loop and is a low performing construction.">count($values) >= get($i)</error>; $i++) {}

// Warning (case #3).
for (; <error descr="[EA] 'count(...)' is used in a loop and is a low performing construction.">count($values) >= $i</error>; $i++) {}
