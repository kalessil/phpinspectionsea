<?php

class clazz {

    /**
     * @throws \RuntimeException
     */
    public function throwsRuntime(){
       throw new \RuntimeException();
    }

    /**
     *
     */
    public function func(){
        try {
            try {
                $this->throwsRuntime();
            } catch (\BadMethodCallException $le) {
                throw new \LogicException();
            }
        } catch (\LogicException $e) {
            throw new \InvalidArgumentException();
        }
    }
}