<?php

class TestAssertCount
{
    public function test()
    {
        $this->assertContains('...', []);
        $this->assertContains('...', []);

        $this->assertContains('...', []);
        $this->assertContains('...', []);
        $this->assertNotContains('...', []);
        $this->assertNotContains('...', []);

        $this->assertTrue(in_array('...'));
        $this->assertFalse(in_array('...'));
    }

    public function testWithMessages()
    {
        $this->assertContains('...', [], '');
        $this->assertContains('...', [], '');

        $this->assertContains('...', [], '');
        $this->assertContains('...', [], '');
        $this->assertNotContains('...', [], '');
        $this->assertNotContains('...', [], '');
    }
}