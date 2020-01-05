package bg.sofia.uni.fmi.mjt.autorship.detection.test;

import bg.sofia.uni.fmi.mjt.authorship.detection.AuthorshipDetectorImpl;
import bg.sofia.uni.fmi.mjt.authorship.detection.FeatureType;
import bg.sofia.uni.fmi.mjt.authorship.detection.LinguisticSignature;
import org.junit.Test;

import java.io.*;

import static junit.framework.TestCase.assertEquals;

public class AutorshipDetectionTest {

    public static final double[] WEIGHTS = {11, 33, 50, 0.4, 4};
    InputStream textToTest = new FileInputStream("resources/myTest.txt");

    public AutorshipDetectionTest() throws FileNotFoundException {
    }

    private InputStream signaturesDataset = new FileInputStream("resources/knownSignatures.txt");
    private static final double HAPAX_LEGOMENA_RATIO = 1d;

    @Test
    public void testCalculateHapaxLegomenaRatioAndCalculateSignature() throws IOException {
        AuthorshipDetectorImpl authorshipDetector = new AuthorshipDetectorImpl(signaturesDataset, null);
        LinguisticSignature linguisticSignature = authorshipDetector.calculateSignature(textToTest);
        assertEquals("Hapax Legomena Ratio is 1 ", true,
                (HAPAX_LEGOMENA_RATIO == linguisticSignature.getFeatures().get(FeatureType.HAPAX_LEGOMENA_RATIO)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowingExceptionWhenCalculatingSimilarity() {
        AuthorshipDetectorImpl authorshipDetector = new AuthorshipDetectorImpl(null, WEIGHTS);
        double sum = authorshipDetector.calculateSimilarity(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowingExceptionWhenCalculatingSignature() throws IOException {
        AuthorshipDetectorImpl authorshipDetector = new AuthorshipDetectorImpl(null, WEIGHTS);
        LinguisticSignature linguisticSignature = authorshipDetector.calculateSignature(null);
    }

}
