<?php

class CasesHolder {
    const REGEX1 = '0';
    const REGEX2 = '1';

    public function case() {
        $original   = '/'. self::REGEX1 . self::REGEX2 . '/i';
        $transition = $original;
        preg_match(<weak_warning descr="'i' modifier is ambiguous here (no alphabet characters in given pattern).">$transition</weak_warning>, '...');
    }
}