<?php

class Clazz {
    public function method() {
        $this->assertEquals(
            'expected', 'values', 'message',
            <weak_warning descr="[EA] $delta is deprecated in favor of assertEqualsWithDelta() since PhpUnit 8.">$delta = 0.0</weak_warning>,
            <weak_warning descr="[EA] $maxDepth is deprecated since PhpUnit 8.">$maxDepth = 10</weak_warning>,
            <weak_warning descr="[EA] $canonicalize is deprecated in favor of assertEqualsCanonicalizing() since PhpUnit 8.">$canonicalize = false</weak_warning>,
            <weak_warning descr="[EA] $ignoreCase is deprecated in favor of assertEqualsIgnoringCase() since PhpUnit 8.">$ignoreCase = false</weak_warning>
        );
        $this->assertNotEquals(
            'expected', 'values', 'message',
            <weak_warning descr="[EA] $delta is deprecated in favor of assertNotEqualsWithDelta() since PhpUnit 8.">$delta = 0.0</weak_warning>,
            <weak_warning descr="[EA] $maxDepth is deprecated since PhpUnit 8.">$maxDepth = 10</weak_warning>,
            <weak_warning descr="[EA] $canonicalize is deprecated in favor of assertNotEqualsCanonicalizing() since PhpUnit 8.">$canonicalize = false</weak_warning>,
            <weak_warning descr="[EA] $ignoreCase is deprecated in favor of assertNotEqualsIgnoringCase() since PhpUnit 8.">$ignoreCase = false</weak_warning>
        );
    }
}