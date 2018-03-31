<?php

class CasesHolder {
    public function method() {
        reference(...$arguments);
        reference(...$this->property);
        reference(...[]);
        reference(...array());
        reference(...reference());

        call_user_func_array([], $arguments);
    }
}
