<?php

class UnusedMocksTest
{
    public function doTest()
    {
        <weak_warning descr="[EA] The mock seems to be not used, consider deleting it.">$unusedMock</weak_warning> = $this->createMock(\stdClass::class);
        $unusedMock->expects($this->once())->method($this->anything());

        $usedMock = $this->createMock(\stdClass::class);
        $this->assertNotNull($usedMock);
    }
}