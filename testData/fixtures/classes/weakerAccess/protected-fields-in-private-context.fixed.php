<?php

class ProtectedFieldsInPrivateContext {
    private $private;
    public  $public;

    protected $protectedUsedInPublic;
    protected $protectedUsedInPublicProtected;
    protected $protectedUsedInPublicPrivate;
    protected $protectedUsedInPublicProtectedPrivate;
    protected $protectedUsedInProtected;
    protected $protectedUsedInProtectedPrivate;
    private $protectedUsedInPrivate;

    public function publicMethod() {
        return [
            $this->private,
            $this->public,

            $this->protectedUsedInPublic,
            $this->protectedUsedInPublicProtected,
            $this->protectedUsedInPublicPrivate,
            $this->protectedUsedInPublicProtectedPrivate
        ];
    }

    protected function protectedMethod() {
        return [
            $this->private,
            $this->public,

            $this->protectedUsedInPublic,
            $this->protectedUsedInPublicProtected,
            $this->protectedUsedInPublicPrivate,
            $this->protectedUsedInPublicProtectedPrivate,
            $this->protectedUsedInProtected,
            $this->protectedUsedInProtectedPrivate
        ];
    }

    private function pivateMethod() {
        return [
            $this->private,
            $this->public,

            $this->protectedUsedInPublicPrivate,
            $this->protectedUsedInPublicProtectedPrivate,
            $this->protectedUsedInProtectedPrivate,
            $this->protectedUsedInPrivate
        ];
    }
}

class ProtectedFieldsInMagicContext {
    private $protectedUsedInMagic;

    public function __toString()
    {
        return (string)$this->protectedUsedInMagic;
    }
}