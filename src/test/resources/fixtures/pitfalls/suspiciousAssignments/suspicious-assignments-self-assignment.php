<?php

    <error descr="Related operation being applied to the same variable (probably merging issues).">$i += $i + 2</error>;
    <error descr="Related operation being applied to the same variable (probably merging issues).">$i -= $i - 2</error>;
    <error descr="Related operation being applied to the same variable (probably merging issues).">$i *= $i * 2</error>;
    <error descr="Related operation being applied to the same variable (probably merging issues).">$i /= $i / 2</error>;
    <error descr="Related operation being applied to the same variable (probably merging issues).">$i &= $i & 2</error>;
    <error descr="Related operation being applied to the same variable (probably merging issues).">$i |= $i | 2</error>;
    <error descr="Related operation being applied to the same variable (probably merging issues).">$i ^= $i ^ 2</error>;
    <error descr="Related operation being applied to the same variable (probably merging issues).">$i <<= $i << 2</error>;
    <error descr="Related operation being applied to the same variable (probably merging issues).">$i >>= $i >> 2</error>;
    <error descr="Related operation being applied to the same variable (probably merging issues).">$i %= $i % 2</error>;
    <error descr="Related operation being applied to the same variable (probably merging issues).">$i .= $i . '2'</error>;

    /* false-positives: applying short operations could not produce those cases */
    $i .= $i . '2' . '3';
    $i += ($i + 2);
    $i += ($i) + 2;
