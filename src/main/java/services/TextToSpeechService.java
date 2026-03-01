package services;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TextToSpeechService {

    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private boolean useRealAPI = false;
    private Voice voice;
    private Thread speechThread;

    public TextToSpeechService() {
        try {
            // ✅ CORRECT: Set the FreeTTS voice directory
            System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");

            VoiceManager voiceManager = VoiceManager.getInstance();
            voice = voiceManager.getVoice("kevin16");

            if (voice != null) {
                voice.allocate();
                System.out.println("✅ FreeTTS initialized successfully");
            } else {
                System.out.println("⚠️ FreeTTS voice not found");
            }
        } catch (Exception e) {
            System.out.println("❌ FreeTTS error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void speak(String text, String language) {
        stop();

        if (!language.equals("English") && !language.equals("en")) {
            System.out.println("🔊 [TTS] Non-English language: " + language);
            simulateSpeech(text, language);
            return;
        }

        if (voice != null) {
            speechThread = new Thread(() -> {
                try {
                    isPlaying = true;
                    System.out.println("🔊 [TTS] Speaking: " + text.substring(0, Math.min(30, text.length())) + "...");
                    voice.speak(text);
                } catch (Exception e) {
                    System.out.println("❌ TTS error: " + e.getMessage());
                } finally {
                    isPlaying = false;
                    System.out.println("⏹️ [TTS] Finished");
                }
            });
            speechThread.setDaemon(true);
            speechThread.start();
        } else {
            simulateSpeech(text, language);
        }
    }

    private void simulateSpeech(String text, String language) {
        System.out.println("🔊 [SIMULATION] Speaking in " + language + ": " + text);

        // Make a beep so you know it's working
        java.awt.Toolkit.getDefaultToolkit().beep();

        isPlaying = true;

        speechThread = new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                // Thread interrupted
            } finally {
                isPlaying = false;
                System.out.println("⏹️ [SIMULATION] Finished");
            }
        });
        speechThread.setDaemon(true);
        speechThread.start();
    }

    public void stop() {
        if (voice != null && isPlaying) {
            try {
                voice.getAudioPlayer().cancel();
            } catch (Exception e) {
                // Ignore
            }
        }

        if (speechThread != null && speechThread.isAlive()) {
            speechThread.interrupt();
        }

        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        isPlaying = false;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    private void playAudioFromBytes(byte[] audioData) {
        try {
            Path tempFile = Files.createTempFile("mentis-tts-", ".mp3");
            File audioFile = tempFile.toFile();

            try (FileOutputStream fos = new FileOutputStream(audioFile)) {
                fos.write(audioData);
            }

            Media media = new Media(audioFile.toURI().toString());
            mediaPlayer = new MediaPlayer(media);

            mediaPlayer.setOnReady(() -> {
                mediaPlayer.play();
                isPlaying = true;
                System.out.println("▶️ Playing audio");
            });

            mediaPlayer.setOnEndOfMedia(() -> {
                isPlaying = false;
                mediaPlayer.dispose();
                audioFile.delete();
                System.out.println("⏹️ Audio finished");
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}