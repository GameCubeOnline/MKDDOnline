package com.kirbymimi.mmb.ut.consumers;

@FunctionalInterface
public interface TriFunction<T, U, R, S> {
   R apply(T var1, U var2, S var3);
}
