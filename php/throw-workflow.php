<?php

class clazz {

    /**
     * @throws \RuntimeException
     */
    public function __construct() {
        throw new \RuntimeException();
    }

    /**
     * @return clazz
     */
    static public function create() {
        return new self();
    }

    /**
     * @return string
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

    /**
     * @return string
     */
    public function __toString() {
        return $this->throwsRuntime();
    }
}