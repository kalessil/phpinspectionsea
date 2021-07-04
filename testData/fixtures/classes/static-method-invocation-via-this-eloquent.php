<?php

namespace Illuminate\Database\Eloquent {
    class Model
    {
        public static function whereIn($column, $values, $boolean = 'and', $not = false) {
        }

        public function test () {
            $this->relation->whereIn('...', []);
            $this->whereIn('...', []);
        }
    }
}