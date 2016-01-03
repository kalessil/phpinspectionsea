<?php

class A
{
    /**
     * @var array
     */
    private $limit;

    public function check()
    {
        $pool = [];

        for ($i = 0; $i < count($pool); ++$i) {
            echo 1;
        }

        for ($i = 0; count($pool) >= $i; ++$i) {
            echo 2;
        }

        for ($this->limit['x'] = 0; $this->limit['x'] < count($pool); ++$this->limit['x']) {
            echo 3;
        }
    }
}