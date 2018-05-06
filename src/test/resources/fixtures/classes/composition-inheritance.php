<?php

interface _Interface {
    public function method();
}


class <warning descr="The class needs to be either final (for aggregation) or abstract (for inheritance).">_ClassFollowsContract</warning> implements _Interface {
    public function method() {}
}

class _ClassBreakContract implements _Interface {
    public function method() {}
    public function breakContract() {}
}


class <warning descr="The class needs to be abstract (since it has children).">_ParentFollowsContract</warning> implements _Interface {
    public function method() {}
}

class <warning descr="The class needs to be either final (for aggregation) or abstract (for inheritance).">_ChildFollowsContract</warning> extends _ParentFollowsContract {}
