<?php

class CasesHolder {
    public function method() {
        <warning descr="'reference(...$arguments)' should be used instead (3x+ faster)">call_user_func_array('reference', $arguments)</warning>;
        <warning descr="'reference(...$this->property)' should be used instead (3x+ faster)">call_user_func_array('reference', $this->property)</warning>;
        <warning descr="'reference(...[])' should be used instead (3x+ faster)">call_user_func_array('reference', [])</warning>;
        <warning descr="'reference(...array())' should be used instead (3x+ faster)">call_user_func_array('reference', array())</warning>;
        <warning descr="'reference(...reference())' should be used instead (3x+ faster)">call_user_func_array('reference', reference())</warning>;

        call_user_func_array([], $arguments);
    }
}
