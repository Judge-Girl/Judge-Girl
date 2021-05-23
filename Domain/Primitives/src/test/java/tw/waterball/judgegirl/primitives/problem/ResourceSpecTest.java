package tw.waterball.judgegirl.primitives.problem;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ResourceSpecTest {

    @Test
    void RequiringManyCPUsShouldBeInvalid() {
        assertThrows(RuntimeException.class,
                () -> new ResourceSpec(30, 0));
    }

    @Test
    void RequiringManyGPUsShouldBeInvalid() {
        assertThrows(RuntimeException.class,
                () -> new ResourceSpec(0, 30));
    }

}