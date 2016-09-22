package com.dxm.rxbinder;

import java.io.Serializable;

/**
 * Created by ants on 9/22/16.
 */

public class Pair<A, B> implements Serializable {

    private final A first;
    private final B second;

    public A first() {
        return first;
    }

    public B second() {
        return second;
    }

    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public String toString() {
        return "Pair(" + first + ", " + second + ")";
    }

    @Override
    public int hashCode() {
        return (null == first ? 0 : first.hashCode() * 13) + (null == second ? 0 : second.hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Pair) {
            final Pair other = (Pair) o;
            return null == first ? null == other.first : first.equals(other.first) && (null == second ? null == other.second : second.equals(other.second));
        }
        return false;
    }
}

