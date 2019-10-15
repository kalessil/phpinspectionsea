<?php

$x = [0, 1 <weak_warning descr="[EA] Can be safely dropped. The comma will be ignored by PHP.">,</weak_warning> ];
$x = array(0, 1 <weak_warning descr="[EA] Can be safely dropped. The comma will be ignored by PHP.">,</weak_warning> );
$x = [
    0,
    1 <weak_warning descr="[EA] Can be safely dropped. The comma will be ignored by PHP.">,</weak_warning>
];
