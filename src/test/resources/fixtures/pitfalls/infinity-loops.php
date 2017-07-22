<?php

class CaseHolder {
    private function methodWithReturn() {
        return <error descr="Causes infinity loop.">$this->methodWithReturn()</error>;
    }
    private function methodWithCall() {
        <error descr="Causes infinity loop.">self::methodWithCall()</error>;
    }
}