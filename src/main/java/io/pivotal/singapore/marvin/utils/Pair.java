package io.pivotal.singapore.marvin.utils;

import java.util.Objects;

public class Pair<A, B> {
    public final A first;
    public final B last;

    public Pair(A var1, B var2) {
        this.first = var1;
        this.last = var2;
    }

    public String toString() {
        return "Pair[" + this.first + "," + this.last + "]";
    }

    public boolean equals(Object var1) {
        return var1 instanceof Pair && Objects.equals(this.first, ((Pair)var1).first) && Objects.equals(this.last, ((Pair)var1).last);
    }

    public int hashCode() {
        return this.first == null?(this.last == null?0:this.last.hashCode() + 1):(this.last == null?this.first.hashCode() + 2:this.first.hashCode() * 17 + this.last.hashCode());
    }

    public static <A, B> Pair<A, B> of(A var0, B var1) {
        return new Pair(var0, var1);
    }
}
