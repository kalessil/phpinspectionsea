<?php

substr($path, 0,                   strlen($path) - strlen($pathPrefix)); // <- reported:  $path, 0, -strlen($pathPrefix)
substr($path, strlen($pathPrefix), strlen($path) - strlen($pathPrefix)); // <- reported:  $path, strlen($pathPrefix)
