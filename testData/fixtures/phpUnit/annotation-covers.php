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

        /**
          * Description here
          * @covers MyIterator::offsetUnset()
          */
        public function testOffsetUnset2() {}

        /** @covers MyIterator::offsetUnzet */
        public function <error descr="@covers referencing to a non-existing entity 'MyIterator::offsetUnzet'">testOffsetUnset3</error>() {}

        /** @covers \PUT\Covers\Annotation\MyArrayIterator::offsetUnset */
        public function testOffsetUnset4() {}

        /** @covers ::trim */
        public function testOffsetUnset5() {}

        /** @covers MyIterator::<!private> */
        public function testOffsetUnset6() {}

        /** whatever @covers whatever */
        public function case_covers_mentioned_one_liner() {}

        /**
         * whatever @covers whatever
         */
        public function case_covers_mentioned_multi_liner() {}
    }
}