<?php
    if ($x)     <error descr="[EA] Probably a bug, because ';' treated as body.">;</error>
    elseif ($y) <error descr="[EA] Probably a bug, because ';' treated as body.">;</error>
    else        <error descr="[EA] Probably a bug, because ';' treated as body.">;</error>

    do                 <error descr="[EA] Probably a bug, because ';' treated as body.">;</error> while($x);
    while ($x)         <error descr="[EA] Probably a bug, because ';' treated as body.">;</error>
    for (;;)           <error descr="[EA] Probably a bug, because ';' treated as body.">;</error>
    foreach ([] as $v) <error descr="[EA] Probably a bug, because ';' treated as body.">;</error>