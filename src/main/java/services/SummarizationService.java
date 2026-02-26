package services;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import java.io.InputStream;
import java.util.*;

/**
 * Local Text Summarization Service using OpenNLP
 * 100% offline, no API keys, no internet required
 */
public class SummarizationService {

    private static SentenceDetectorME sentenceDetector;
    private static boolean isInitialized = false;

    static {
        try {
            // Load the sentence detection model
            InputStream modelStream = SummarizationService.class
                    .getResourceAsStream("/models/en-sent.bin");

            if (modelStream != null) {
                SentenceModel model = new SentenceModel(modelStream);
                sentenceDetector = new SentenceDetectorME(model);
                isInitialized = true;
                System.out.println("✅ SummarizationService initialized successfully");
            } else {
                System.err.println("❌ Could not find en-sent.bin model file");
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize SummarizationService: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generate a summary by extracting the most important sentences
     * @param text Full text to summarize
     * @param maxSentences Maximum number of sentences in summary
     * @return Summarized text
     */
    public static String summarize(String text, int maxSentences) {
        if (!isInitialized || text == null || text.isEmpty()) {
            return text;
        }

        try {
            // Detect sentences
            String[] sentences = sentenceDetector.sentDetect(text);

            if (sentences.length <= maxSentences) {
                return text;
            }

            // Simple extractive summarization - take first N sentences
            // (You can make this smarter by scoring sentences)
            StringBuilder summary = new StringBuilder();
            for (int i = 0; i < Math.min(maxSentences, sentences.length); i++) {
                summary.append(sentences[i]).append(" ");
            }

            return summary.toString().trim();

        } catch (Exception e) {
            System.err.println("❌ Summarization error: " + e.getMessage());
            return text.length() > 200 ? text.substring(0, 197) + "..." : text;
        }
    }

    /**
     * Smart summarization that picks the most important sentences
     * based on keyword frequency
     */
    public static String smartSummarize(String text, int maxSentences) {
        if (!isInitialized || text == null || text.isEmpty()) {
            return text;
        }

        try {
            String[] sentences = sentenceDetector.sentDetect(text);

            if (sentences.length <= maxSentences) {
                return text;
            }

            // Score sentences based on important words
            Map<String, Integer> wordScores = calculateWordScores(text);
            List<SentenceScore> scoredSentences = new ArrayList<>();

            for (int i = 0; i < sentences.length; i++) {
                double score = calculateSentenceScore(sentences[i], wordScores);
                scoredSentences.add(new SentenceScore(sentences[i], score, i));
            }

            // Sort by score (highest first) and pick top sentences
            scoredSentences.sort((a, b) -> Double.compare(b.score, a.score));

            List<SentenceScore> topSentences = scoredSentences.subList(0,
                    Math.min(maxSentences, scoredSentences.size()));

            // Sort back to original order
            topSentences.sort(Comparator.comparingInt(a -> a.position));

            StringBuilder summary = new StringBuilder();
            for (SentenceScore ss : topSentences) {
                summary.append(ss.sentence).append(" ");
            }

            return summary.toString().trim();

        } catch (Exception e) {
            System.err.println("❌ Smart summarization error: " + e.getMessage());
            return summarize(text, maxSentences);
        }
    }

    private static Map<String, Integer> calculateWordScores(String text) {
        Map<String, Integer> scores = new HashMap<>();
        String[] words = text.toLowerCase().split("\\W+");

        // Common stop words to ignore
        Set<String> stopWords = new HashSet<>(Arrays.asList(
                "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for",
                "of", "with", "by", "from", "up", "about", "into", "through", "during",
                "before", "after", "while", "as", "until", "is", "are", "was", "were",
                "be", "been", "being", "have", "has", "had", "do", "does", "did"
        ));

        for (String word : words) {
            if (!stopWords.contains(word) && word.length() > 2) {
                scores.put(word, scores.getOrDefault(word, 0) + 1);
            }
        }

        return scores;
    }

    private static double calculateSentenceScore(String sentence, Map<String, Integer> wordScores) {
        String[] words = sentence.toLowerCase().split("\\W+");
        double score = 0;

        for (String word : words) {
            score += wordScores.getOrDefault(word, 0);
        }

        // Longer sentences get slightly higher score
        score += words.length * 0.1;

        return score;
    }

    private static class SentenceScore {
        String sentence;
        double score;
        int position;

        SentenceScore(String sentence, double score, int position) {
            this.sentence = sentence;
            this.score = score;
            this.position = position;
        }
    }

    /**
     * Check if service is initialized
     */
    public static boolean isInitialized() {
        return isInitialized;
    }
}