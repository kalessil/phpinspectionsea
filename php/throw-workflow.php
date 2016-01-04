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
        return new self();  // <- weak warning
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
            $this->throwsRuntime(); // <- weak warning
            try {
                $this->throwsRuntime(); // <- this needs to be reported
            } catch (\BadMethodCallException $le) {
                throw new \LogicException();
            }
        } catch (\LogicException $e) {
            throw new \InvalidArgumentException(); // <- weak warning
        }
    }

    /**
     * @return string
     */
    public function __toString() {
        return $this->throwsRuntime(); // <- error
    }


    /**
     * @return void
     */
    public function throwVariable() {
        $e = new \RuntimeException();
        throw $e; // <- weak warning
    }
}