<?php

trait _Trait             {}
interface _Interface     {}

abstract class _Abstract {}
final class _Final       {}

class <warning descr="[EA] The class needs to be abstract (since it has children).">_Parent</warning> {}
class <warning descr="[EA] The class needs to be either final (for aggregation) or abstract (for inheritance).">_Child</warning> extends _Parent {}

/** @ORM\Entity */
class AnnotatedClass extends _Parent {}