<?php

/** @property mixed $annotatedProperty */
class CasesHolder
{
    protected $reported;
    protected $trigger;
    public function <warning descr="It's probably a wrong field was used here ('reported' could fit).">isReported</warning>()
    {
        return $this->trigger;
    }
    public function <warning descr="It's probably a wrong field was used here ('reported' could fit).">getReported</warning>()
    {
        return $this->trigger;
    }
    public function <warning descr="It's probably a wrong field was used here ('reported' could fit).">setReported</warning>()
    {
        $this->trigger = '...';
    }

    /* the matched property is from PhpDoc */
    public function getAnnotatedProperty() { return $this->trigger; }

    /* multiple parameters, hardly setter/getter */
    protected $skipped;
    public function isSkipped($one, $two) { return $this->trigger; }

    protected $correct;
    public function isCorrect()  { return $this->correct;  }
    public function getCorrect() { return $this->correct;  }
    public function setCorrect() { $this->correct = '...'; }
}