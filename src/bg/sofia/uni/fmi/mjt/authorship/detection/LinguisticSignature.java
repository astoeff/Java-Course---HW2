package bg.sofia.uni.fmi.mjt.authorship.detection;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class LinguisticSignature {
    private Map<FeatureType, Double> features = new TreeMap<>() {
    };

    public LinguisticSignature(Map<FeatureType, Double> features) {
        this.features = features;
    }

    public Map<FeatureType, Double> getFeatures() {
        return features;
    }
}
