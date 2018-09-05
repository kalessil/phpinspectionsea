<?php

namespace {
    class PHPUnit_Framework_TestCase {
        static public function assertSame($first, $second) {}
    }

    class TestSuit extends PHPUnit_Framework_TestCase {
        public function testSomething() {
            $this->assertSame(true, false);
        }
    }
}

namespace PHPUnit\Framework {
    class TestCase {
        static public function assertSame($first, $second) {}
    }

    class TestSuit extends TestCase {
        public function testSomething() {
            $this->assertSame(true, false);
        }
    }
}