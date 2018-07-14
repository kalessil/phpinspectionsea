<?php

    preg_match(<weak_warning descr="'i' modifier is ambiguous here (no alphabet characters in given pattern).">'/123.+/i'</weak_warning>, '...');
    preg_match(<weak_warning descr="'i' modifier is ambiguous here (no alphabet characters in given pattern).">'{123.+}i'</weak_warning>, '...');
    preg_match(<weak_warning descr="'i' modifier is ambiguous here (no alphabet characters in given pattern).">'<123.+>i'</weak_warning>, '...');
    preg_match(<weak_warning descr="'i' modifier is ambiguous here (no alphabet characters in given pattern).">'(123.+)i'</weak_warning>, '...');
    preg_match(<weak_warning descr="'i' modifier is ambiguous here (no alphabet characters in given pattern).">'[123.+]i'</weak_warning>, '...');