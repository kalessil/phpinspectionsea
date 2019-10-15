<?php

    preg_match(<weak_warning descr="[EA] Leading .* can be probably removed.">'/.*-report/'</weak_warning>, '');
    preg_match('/.*-report/', '', $m);
    preg_match(<weak_warning descr="[EA] Trailing .* can be probably removed.">'/report-.*/'</weak_warning>, '');
    preg_replace('/report-.*/', '', '');
    preg_match('/report-.*/', '', $m);
    preg_replace('/report-.*/', '', '');
    preg_match('/.*(no-report)-.*\0/', '');
    preg_match('/^.*-no-report/', '');
    preg_match('/no-report-.*$/', '');