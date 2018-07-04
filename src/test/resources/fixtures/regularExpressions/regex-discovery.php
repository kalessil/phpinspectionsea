<?php

class CasesHolder {
    const REGEX1 = '123';
    const REGEX2 = '.+';

    public function case() {
        preg_match(<weak_warning descr="'i' modifier is ambiguous here (no alphabet characters in given pattern).">'/'. self::REGEX1 . self::REGEX2 . '/i'</weak_warning>, '...');
    }
}