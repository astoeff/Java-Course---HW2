package bg.sofia.uni.fmi.mjt.authorship.detection;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class AuthorshipDetectorImpl implements AuthorshipDetector {

    private double[] weights;
    private InputStream signaturesDateset;

    public AuthorshipDetectorImpl(InputStream signaturesDataset, double[] weights) {
        this.weights = weights;
        this.signaturesDateset = signaturesDataset;
    }

    public static String cleanUp(String word) {
        return word.toLowerCase()
                .replaceAll("^[!.,:;\\-?<>#\\*\'\"\\[\\(\\]\\)\\n\\t\\\\]+" +
                                "|[!.,:;\\-?<>#\\*\'\"\\[\\(\\]\\)\\n\\t\\\\]+$",
                        "");
    }

    public static boolean isCharPhraseDelimiter(char c) {
        return c == ',' || c == ':' || c == ';';
    }

    public static boolean isCharEndOfSentence(char c) {
        return (c == '.' || c == '!' || c == '?' || c == ' ');
    }

    public static boolean isCharSpace(char c) {
        return c == ' ';
    }

    public static Set<String> findAllDifferentWordsFromList(List<String> words) {
        Set<String> allDifferentWords = new HashSet<>();
        ListIterator<String>
                iterator = words.listIterator();
        while (iterator.hasNext()) {
            allDifferentWords.add(iterator.next());
        }
        return allDifferentWords;
    }

    public static Set<String> findAllUniqueWordsFromList(List<String> words) {
        Set<String> usedWords = new HashSet<>();
        Set<String> uniqueWords = new HashSet<>();
        ListIterator<String>
                iterator = words.listIterator();
        while (iterator.hasNext()) {
            String currentWord = iterator.next();
            if (!usedWords.add(currentWord)) {
                uniqueWords.remove(currentWord);
            } else {
                uniqueWords.add(currentWord);
            }
        }
        return uniqueWords;
    }

    /**
     * Returns the linguistic signature for the given input stream @mysteryText based on the following features:
     * 1. Average Word Complexity
     * 2. Type Token Ratio
     * 3. Hapax Legomena Ratio
     * 4. Average Sentence Length
     * 5. Average Sentence Complexity
     *
     * @throws IllegalArgumentException if @mysteryText is null
     */
    @Override
    public LinguisticSignature calculateSignature(InputStream mysteryText) throws IOException {
        if (mysteryText != null) {
            int data;
            long countCharacters = 0;
            long countWords = 0;
            long countPhrases = 0;
            long countSentences = 0;
            boolean isAlreadyWord = false;
            boolean isAlreadyPhrase = false;
            boolean isAlreadySentence = false;
            List<String> words = new ArrayList<>();
            List<String> phrases = new ArrayList<>();
            List<String> sentences = new ArrayList<>();
            String currWord = new String();
            String currPhrase = new String();
            String currSentence = new String();
            while ((data = mysteryText.read()) != -1) {
                char currSymbol = (char) (data);                     //chetem simvol
                countCharacters++;                                   // chars++
                if (isCharSpace(currSymbol) || currSymbol == '\n') {                       //ako e ' '
                    if (isAlreadyWord) {                               //veche ima duma
                        isAlreadyWord = false;
                        currWord = cleanUp(currWord);
                        if (!currWord.matches("^[!.,:;\\-?<>#\\*\'\"\\[\\(\\]\\)\\n\\t\\\\]*$") && currWord.isEmpty()) {
                            words.add(currWord);
                            countWords++;
                        }
                        currWord = new String();
                    } else {                                            //nqma duma -->nishto ne stava
                    }
                } else {                                                //ne e ' '
                    isAlreadyWord = true;
                    currWord += currSymbol;
                }

                if (isCharPhraseDelimiter(currSymbol)) {                       //ako e , : ;
                    if (isAlreadyPhrase) {                               //veche ima phraza
                        countPhrases++;
                        currPhrase.replace('\n', ' ');
                        phrases.add(currPhrase);
                        currPhrase = new String();
                        isAlreadyPhrase = false;
                    } else {                                            //nqma phraza -->nishto ne stava
                    }
                } else {                                                //ne e , ; :
                    isAlreadyPhrase = true;
                    currPhrase += currSymbol;
                }

                if (isCharEndOfSentence(currSymbol)) {                 //ako e . ? !
                    if (isAlreadySentence) {                               //veche ima izrechenie
                        if (currSymbol != ' ') {
                            countSentences++;
                            isAlreadySentence = false;
                            sentences.add(currSentence);
                            currSentence = new String();
                            currPhrase = new String();
                        } else {
                            currSentence += currSymbol;
                        }
                    } else {                                            //nqma izrechenie -->nishto ne stava
                    }
                } else {                                                //ne e . ? !
                    isAlreadySentence = true;
                    currSentence += currSymbol;
                }
            }
            if (isAlreadyWord) {
                countWords++;
                words.add(currWord);
            }
            if (isAlreadyPhrase) {
                countPhrases++;
                phrases.add(currPhrase);
            }
            if (isAlreadySentence) {
                countSentences++;
                sentences.add(currSentence);
            }
            Map<FeatureType, Double> currentValues = new HashMap<>();
            currentValues.put(FeatureType.AVERAGE_WORD_LENGTH, words.size() / (double) (countCharacters));
            currentValues.put(FeatureType.TYPE_TOKEN_RATIO, (double) (findAllDifferentWordsFromList(words).size()));
            currentValues.put(FeatureType.HAPAX_LEGOMENA_RATIO, (double) (findAllUniqueWordsFromList(words).size()));
            currentValues.put(FeatureType.AVERAGE_SENTENCE_LENGTH, words.size() / (double) (sentences.size()));
            currentValues.put(FeatureType.AVERAGE_SENTENCE_COMPLEXITY, phrases.size() / (double) (sentences.size()));
            return new LinguisticSignature(currentValues);
        }
        throw new IllegalArgumentException();
    }

    @Override
    public double calculateSimilarity(LinguisticSignature firstSignature, LinguisticSignature secondSignature) {
        if (firstSignature == null || secondSignature == null) {
            throw new IllegalArgumentException();
        }
        Map<FeatureType, Double> firstSignatureMap = firstSignature.getFeatures();
        Map<FeatureType, Double> secondSignatureMap = secondSignature.getFeatures();
        FeatureType[] allFeatureTypes = FeatureType.values();
        int pos = 0;
        double sum = 0d;
        for (FeatureType feature :
                allFeatureTypes) {
            double featureFromFirstSignature = firstSignatureMap.get(feature);
            double featureFromSecondSignature = secondSignatureMap.get(feature);
            sum += Math.abs(featureFromFirstSignature - featureFromSecondSignature) * weights[pos++];
        }
        return sum;
    }

    @Override
    public String findAuthor(InputStream mysteryText) {
        if (mysteryText == null) {
            throw new IllegalArgumentException();
        }

        return null;
    }
}
