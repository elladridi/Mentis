package services;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import javafx.application.Platform;

/**
 * Local Text-to-Speech service using FreeTTS
 * 100% offline, no API keys, no internet required
 */
public class LocalTTSService {

    private static Voice voice;
    private static boolean isSpeaking = false;
    private static Thread speakingThread;

    static {
        // Initialize FreeTTS
        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        VoiceManager voiceManager = VoiceManager.getInstance();

        // Get available voices (different method)
        com.sun.speech.freetts.Voice[] voices = voiceManager.getVoices();
        System.out.println("✅ Available TTS voices:");
        for (com.sun.speech.freetts.Voice v : voices) {
            System.out.println("   - " + v.getName());
        }

        // Use kevin16 voice (most common)
        voice = voiceManager.getVoice("kevin16");

        if (voice != null) {
            voice.allocate();
            System.out.println("✅ FreeTTS initialized successfully with voice: kevin16");
        } else {
            System.err.println("❌ Failed to initialize FreeTTS");
            // Try alternative voice
            voice = voiceManager.getVoice("kevin");
            if (voice != null) {
                voice.allocate();
                System.out.println("✅ FreeTTS initialized with alternative voice: kevin");
            }
        }
    }

    /**
     * Speak text directly (blocks until finished)
     */
    public static void speak(String text) {
        if (voice == null) {
            System.err.println("❌ TTS not initialized");
            return;
        }

        try {
            voice.speak(text);
        } catch (Exception e) {
            System.err.println("❌ TTS error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Speak text in background thread (non-blocking)
     */
    public static void speakAsync(String text, Runnable onComplete) {
        if (voice == null) {
            System.err.println("❌ TTS not initialized");
            return;
        }

        // Stop any ongoing speech
        stopSpeaking();

        speakingThread = new Thread(() -> {
            isSpeaking = true;
            try {
                voice.speak(text);
            } catch (Exception e) {
                System.err.println("❌ TTS error: " + e.getMessage());
                e.printStackTrace();
            } finally {
                isSpeaking = false;
                if (onComplete != null) {
                    Platform.runLater(onComplete);
                }
            }
        });

        speakingThread.setDaemon(true);
        speakingThread.start();
    }

    /**
     * Stop current speech
     */
    public static void stopSpeaking() {
        if (isSpeaking && speakingThread != null) {
            speakingThread.interrupt();
            isSpeaking = false;
        }
    }

    /**
     * Check if currently speaking
     */
    public static boolean isSpeaking() {
        return isSpeaking;
    }

    /**
     * Get available voices as array of strings
     */
    public static String[] getAvailableVoices() {
        VoiceManager voiceManager = VoiceManager.getInstance();
        com.sun.speech.freetts.Voice[] voices = voiceManager.getVoices();
        String[] voiceNames = new String[voices.length];

        for (int i = 0; i < voices.length; i++) {
            voiceNames[i] = voices[i].getName();
        }

        return voiceNames;
    }

    /**
     * Change voice
     */
    public static boolean setVoice(String voiceName) {
        VoiceManager voiceManager = VoiceManager.getInstance();
        Voice newVoice = voiceManager.getVoice(voiceName);

        if (newVoice != null) {
            if (voice != null) {
                voice.deallocate();
            }
            voice = newVoice;
            voice.allocate();
            System.out.println("✅ Switched to voice: " + voiceName);
            return true;
        }
        System.out.println("❌ Voice not found: " + voiceName);
        return false;
    }

    /**
     * Clean up resources
     */
    public static void shutdown() {
        stopSpeaking();
        if (voice != null) {
            voice.deallocate();
        }
    }
}