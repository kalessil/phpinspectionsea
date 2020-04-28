<?php

class Clazz {
    public function method() {
        $this->assertEquals(
            'expected', 'values', 'message',
            <weak_warning descr="[EA] $delta is deprecated in favor of assertEqualsWithDelta() since PHPUnit 8.">$delta = 0.0</weak_warning>,
            <weak_warning descr="[EA] $maxDepth is deprecated since PHPUnit 8.">$maxDepth = 10</weak_warning>,
            <weak_warning descr="[EA] $canonicalize is deprecated in favor of assertEqualsCanonicalizing() since PHPUnit 8.">$canonicalize = false</weak_warning>,
            <weak_warning descr="[EA] $ignoreCase is deprecated in favor of assertEqualsIgnoringCase() since PHPUnit 8.">$ignoreCase = false</weak_warning>
        );
        $this->assertNotEquals(
            'expected', 'values', 'message',
            <weak_warning descr="[EA] $delta is deprecated in favor of assertNotEqualsWithDelta() since PHPUnit 8.">$delta = 0.0</weak_warning>,
            <weak_warning descr="[EA] $maxDepth is deprecated since PHPUnit 8.">$maxDepth = 10</weak_warning>,
            <weak_warning descr="[EA] $canonicalize is deprecated in favor of assertNotEqualsCanonicalizing() since PHPUnit 8.">$canonicalize = false</weak_warning>,
            <weak_warning descr="[EA] $ignoreCase is deprecated in favor of assertNotEqualsIgnoringCase() since PHPUnit 8.">$ignoreCase = false</weak_warning>
        );
    }
}