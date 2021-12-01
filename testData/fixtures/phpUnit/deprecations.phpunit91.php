<?php

class Clazz {
    public function method() {
        $this-><weak_warning descr="[EA] assertFileNotExists is deprecated in favor of assertFileDoesNotExists() since PHPUnit 9.1.">assertFileNotExists</weak_warning>('');
        $this-><weak_warning descr="[EA] assertDirectoryNotExists is deprecated in favor of assertDirectoryDoesNotExists() since PHPUnit 9.1.">assertDirectoryNotExists</weak_warning>('');
    }
}