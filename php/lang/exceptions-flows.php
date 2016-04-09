<?php

class ExceptionsFlows
{
    /**
     * @throws \RuntimeException
     */
    private function provokeException()
    {
    }

    /**
     * @throws \PHPUnit_Framework_AssertionFailedError
     */
    private function assertSomething()
    {
    }

    /**
     */
    public function api()
    {
        $this->assertSomething();
        $this->provokeException();                     // <- reported
        try {
            try {
                $this->provokeException();
            } catch (\RuntimeException $e1) {
                throw new \UnexpectedValueException(); // <- not analyzed so deeply yet
            }

            throw new \UnexpectedValueException();     // <- reported
        } catch (\InvalidArgumentException $e2) {
            throw new \UnexpectedValueException();     // <- reported
        }
    }
}