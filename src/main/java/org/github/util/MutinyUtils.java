package org.github.util;

import io.smallrye.mutiny.Uni;

import java.util.List;

public final class MutinyUtils {

    private MutinyUtils() {
    }

    // Uni.join().all() does not accept an empty list, so that case is short-circuited first.
    // Otherwise, Uni.join().all(...).andFailFast() subscribes to all the Unis at once (real
    // fan-out) and returns a Uni<List<T>> preserving the input order. It is the Mutiny
    // equivalent of RxJava's concatMapEager.
    public static <T> Uni<List<T>> joinAll(List<Uni<T>> unis) {
        if (unis.isEmpty()) {
            return Uni.createFrom().item(List.of());
        }
        return Uni.join().all(unis).andFailFast();
    }
}