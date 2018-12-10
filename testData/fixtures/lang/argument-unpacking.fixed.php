<?php

class CasesHolder {
    public function method() {
        reference(...$arguments);
        reference(...$this->property);
        reference(...[]);
        reference(...array());
        reference(...reference());

        array_merge(...$arguments);
        \array_merge(...$arguments);

        call_user_func_array([], $arguments);
    }
}
