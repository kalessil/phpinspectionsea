<?php

trait _Trait             {}
interface _Interface     {}

abstract class _Abstract {}
final class _Final       {}

class <weak_warning descr="The class needs to be abstract (since it has children).">_Parent</weak_warning> {}
class <weak_warning descr="The class needs to be either final (for aggregation) or abstract (for inheritance).">_Child</weak_warning> extends _Parent {}