<?php

namespace PUT\Covers\Annotation {
    class MyArrayIterator extends \ArrayIterator {
    }
}

namespace {
    use \PUT\Covers\Annotation\MyArrayIterator as MyIterator;

    class PUTCoversAnnotation {
        /** @covers \ArrayIterator::offsetExists */
        public function testOffsetExists() {}

        /** @covers MyIterator::offsetUnset */
        public function testOffsetUnset1() {}

        /** @covers MyIterator::offsetUnset() */
        public function testOffsetUnset2() {}

        /** @covers \PUT\Covers\Annotation\MyArrayIterator::offsetUnset */
        public function testOffsetUnset3() {}
    }
}