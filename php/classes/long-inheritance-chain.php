<?php

/* @deprecated */
class first
{
}

/* @deprecated */
class second extends first
{
}

/* @deprecated */
class third extends second
{
}

/* @deprecated */
class forthNotReported extends third
{
}

class forthReported extends third
{
}