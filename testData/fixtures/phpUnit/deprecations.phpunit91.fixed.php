<?php

class Clazz {
    public function method() {
        $this->assertFileDoesNotExists('');
        $this->assertDirectoryDoesNotExists('');
    }
}