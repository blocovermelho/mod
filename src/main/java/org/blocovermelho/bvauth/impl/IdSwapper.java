package org.blocovermelho.bvauth.impl;

import java.util.UUID;

public interface IdSwapper {
    default void bv$swapId(UUID old, UUID _new) {}
}
