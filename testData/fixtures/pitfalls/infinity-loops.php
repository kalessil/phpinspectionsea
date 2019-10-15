<?php

class CaseHolder {
    private function methodWithReturn() {
        return <error descr="[EA] Causes infinity loop.">$this->methodWithReturn()</error>;
    }
    private function methodWithCall() {
        <error descr="[EA] Causes infinity loop.">self::methodWithCall()</error>;
    }
}