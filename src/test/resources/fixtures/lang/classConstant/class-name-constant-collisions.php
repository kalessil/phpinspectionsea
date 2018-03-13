<?php

namespace Package\One {
    class Clazz {
        public function method() {
            return <weak_warning descr="Perhaps this can be replaced with \Package\Two\Clazz::class.">'\Package\Two\Clazz'</weak_warning>;
        }
    }
}

namespace Package\Two {
    class Clazz {
    }
}