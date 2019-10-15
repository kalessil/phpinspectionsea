<?php

class CasesHolder {
    public function method() {
        <warning descr="[EA] 'reference(...$arguments)' would make more sense here (3x+ faster). Use '...array_values(...)' for unpacking associative arrays.">call_user_func_array('reference', $arguments)</warning>;
        <warning descr="[EA] 'reference(...$this->property)' would make more sense here (3x+ faster). Use '...array_values(...)' for unpacking associative arrays.">call_user_func_array('reference', $this->property)</warning>;
        <warning descr="[EA] 'reference(...[])' would make more sense here (3x+ faster). Use '...array_values(...)' for unpacking associative arrays.">call_user_func_array('reference', [])</warning>;
        <warning descr="[EA] 'reference(...array())' would make more sense here (3x+ faster). Use '...array_values(...)' for unpacking associative arrays.">call_user_func_array('reference', array())</warning>;
        <warning descr="[EA] 'reference(...reference())' would make more sense here (3x+ faster). Use '...array_values(...)' for unpacking associative arrays.">call_user_func_array('reference', reference())</warning>;

        <warning descr="[EA] 'array_merge(...$arguments)' would make more sense here (3x+ faster). Use '...array_values(...)' for unpacking associative arrays.">\call_user_func_array('array_merge', $arguments)</warning>;
        <warning descr="[EA] '\array_merge(...$arguments)' would make more sense here (3x+ faster). Use '...array_values(...)' for unpacking associative arrays.">\call_user_func_array('\\array_merge', $arguments)</warning>;

        call_user_func_array([], $arguments);
    }
}
