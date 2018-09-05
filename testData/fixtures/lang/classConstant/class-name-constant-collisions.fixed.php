<?php

namespace Package\One {
    class Clazz {
        public function method() {
            return \Package\Two\Clazz::class;
        }
    }
}

namespace Package\Two {
    class Clazz {
    }
}