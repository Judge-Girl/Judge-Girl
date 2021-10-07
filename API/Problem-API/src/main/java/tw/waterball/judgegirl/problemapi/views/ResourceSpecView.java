package tw.waterball.judgegirl.problemapi.views;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.primitives.problem.ResourceSpec;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceSpecView {
    public float cpu;
    public float gpu;

    public static ResourceSpecView toViewModel(ResourceSpec spec) {
        return new ResourceSpecView(spec.getCpu(), spec.getGpu());
    }

    public ResourceSpec toValue() {
        return new ResourceSpec(cpu, gpu);
    }
}
