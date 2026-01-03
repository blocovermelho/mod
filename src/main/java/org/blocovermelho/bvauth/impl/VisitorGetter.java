package org.blocovermelho.bvauth.impl;


import java.util.UUID;

public interface VisitorGetter {
    default void bv$setVisitor(UUID id) {}
    default boolean bv$isVisitor(UUID id) {
        throw new UnsupportedOperationException();
    }
    default void bv$unsetVisitor(UUID id) {}
}
