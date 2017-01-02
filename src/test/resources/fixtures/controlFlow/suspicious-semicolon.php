<?php
    if ($x)     <error descr="Probably a bug, because ';' treated as body.">;</error>
    elseif ($y) <error descr="Probably a bug, because ';' treated as body.">;</error>
    else        <error descr="Probably a bug, because ';' treated as body.">;</error>

    do                 <error descr="Probably a bug, because ';' treated as body.">;</error> while($x);
    while ($x)         <error descr="Probably a bug, because ';' treated as body.">;</error>
    for (;;)           <error descr="Probably a bug, because ';' treated as body.">;</error>
    foreach ([] as $v) <error descr="Probably a bug, because ';' treated as body.">;</error>