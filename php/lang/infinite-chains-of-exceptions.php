<?php

abstract class A
{
    /**
     * @throws \InvalidArgumentException
     */
    public abstract function bbb();

    /**
     * @return void
     */
    public function aaa()
    {
        // see https://bugs.php.net/bug.php?id=70944
        $e = new \RuntimeException('Bar');
        try {
            $this->bbb();
        } catch (\RuntimeException $ex) {
            /* do nothing */
        } finally {
            $this->bbb(); // <-- highlighted

            try {
                $this->bbb(); // <-- not highlighted
            } catch (\Exception $ex) {
            }

            throw $e; // <-- highlighted
        }
    }

}