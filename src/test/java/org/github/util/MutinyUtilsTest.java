package org.github.util;

import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MutinyUtilsTest {

    @Test
    void joinAll_emptyList_emitsEmptyListWithoutSubscribing() {
        List<Integer> result = MutinyUtils.<Integer>joinAll(List.of()).await().indefinitely();

        assertThat(result).isEmpty();
    }

    @Test
    void joinAll_preservesInputOrder() {
        List<Integer> result = MutinyUtils.joinAll(List.of(
                        Uni.createFrom().item(1),
                        Uni.createFrom().item(2),
                        Uni.createFrom().item(3)))
                .await().indefinitely();

        assertThat(result).containsExactly(1, 2, 3);
    }

    @Test
    void joinAll_propagatesFailure() {
        RuntimeException boom = new RuntimeException("boom");

        assertThatThrownBy(() -> MutinyUtils.joinAll(List.of(
                        Uni.createFrom().item(1),
                        Uni.createFrom().failure(boom)))
                .await().indefinitely())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("boom");
    }
}