package services;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.photo.Photo;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

public class FaceRecognitionService {

    private static final String FACE_DIR = "face_data/";
    private CascadeClassifier faceDetector;
    private Map<Integer, List<Mat>> userFaceSamples = new HashMap<>();
    private Map<Integer, List<String>> userFacePaths = new HashMap<>();
    private static final int REQUIRED_SAMPLES = 5;

    // Face detection parameters
    private static final double SCALE_FACTOR = 1.1;
    private static final int MIN_NEIGHBORS = 5;
    private static final Size MIN_FACE_SIZE = new Size(100, 100);

    static {
        // Load OpenCV native library
        nu.pattern.OpenCV.loadLocally();
    }

    public FaceRecognitionService() {
        // Load face detection cascade
        String cascadePath = "src/main/resources/haarcascade_frontalface_default.xml";
        faceDetector = new CascadeClassifier(cascadePath);

        if (faceDetector.empty()) {
            System.err.println("❌ Failed to load cascade classifier!");
        }

        // Create face data directory if not exists
        new File(FACE_DIR).mkdirs();

        // Load existing faces
        loadExistingFaces();
    }

    private void loadExistingFaces() {
        File[] faceFiles = new File(FACE_DIR).listFiles((dir, name) ->
                name.endsWith(".png") || name.endsWith(".jpg")
        );

        if (faceFiles != null) {
            for (File faceFile : faceFiles) {
                String[] parts = faceFile.getName().split("_");
                if (parts.length >= 2) {
                    try {
                        int userId = Integer.parseInt(parts[0]);
                        userFacePaths.computeIfAbsent(userId, k -> new ArrayList<>())
                                .add(faceFile.getAbsolutePath());

                        Mat face = Imgcodecs.imread(faceFile.getAbsolutePath(), Imgcodecs.IMREAD_GRAYSCALE);
                        if (!face.empty()) {
                            Mat processed = preprocessFace(face);
                            userFaceSamples.computeIfAbsent(userId, k -> new ArrayList<>()).add(processed);
                        }
                    } catch (NumberFormatException e) {
                        // Skip invalid filenames
                    }
                }
            }
        }

        int totalSamples = 0;
        for (List<Mat> samples : userFaceSamples.values()) {
            totalSamples += samples.size();
        }
        System.out.println("✅ Loaded " + totalSamples + " face samples for " +
                userFaceSamples.size() + " users");
    }

    public boolean registerFaceSample(int userId, File imageFile) {
        try {
            Mat face = detectFace(imageFile);
            if (face == null) {
                System.out.println("❌ No face detected in image");
                return false;
            }

            // Check image quality
            if (!isGoodQuality(face)) {
                System.out.println("❌ Image quality too low, please take another photo");
                return false;
            }

            Mat processedFace = preprocessFace(face);

            String timestamp = String.valueOf(System.currentTimeMillis());
            String facePath = FACE_DIR + userId + "_" + timestamp + ".png";

            Imgcodecs.imwrite(facePath, processedFace);

            userFaceSamples.computeIfAbsent(userId, k -> new ArrayList<>()).add(processedFace);
            userFacePaths.computeIfAbsent(userId, k -> new ArrayList<>()).add(facePath);

            int sampleCount = userFaceSamples.get(userId).size();
            System.out.println("✅ Sample " + sampleCount + " of " + REQUIRED_SAMPLES +
                    " registered for user: " + userId);

            return true;

        } catch (Exception e) {
            System.err.println("❌ Face registration error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean isGoodQuality(Mat face) {
        // Check if image is too blurry using Laplacian variance
        Mat laplacian = new Mat();
        Imgproc.Laplacian(face, laplacian, CvType.CV_64F);
        Core.MinMaxLocResult mm = Core.minMaxLoc(laplacian);
        double variance = mm.maxVal - mm.minVal;

        return variance > 50; // Threshold for acceptable quality
    }

    private Mat detectFace(File imageFile) {
        Mat image = Imgcodecs.imread(imageFile.getAbsolutePath());
        if (image.empty()) {
            System.err.println("❌ Cannot read image: " + imageFile.getAbsolutePath());
            return null;
        }

        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);

        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(grayImage, faceDetections, SCALE_FACTOR,
                MIN_NEIGHBORS, 0, MIN_FACE_SIZE);

        Rect[] faces = faceDetections.toArray();
        if (faces.length == 0) {
            return null;
        }

        // Get the largest face (most likely the main subject)
        Rect largestFace = faces[0];
        for (Rect face : faces) {
            if (face.area() > largestFace.area()) {
                largestFace = face;
            }
        }

        return new Mat(grayImage, largestFace);
    }

    private Mat preprocessFace(Mat face) {
        // Resize to standard size
        Mat resized = new Mat();
        Size standardSize = new Size(200, 200);
        Imgproc.resize(face, resized, standardSize);

        // Apply histogram equalization to normalize lighting
        Mat equalized = new Mat();
        Imgproc.equalizeHist(resized, equalized);

        // Apply CLAHE (Contrast Limited Adaptive Histogram Equalization)
        Mat claheOutput = new Mat();
        CLAHE clahe = Imgproc.createCLAHE(2.0, new Size(8, 8));
        clahe.apply(equalized, claheOutput);

        // Apply slight Gaussian blur to reduce noise
        Mat blurred = new Mat();
        Imgproc.GaussianBlur(claheOutput, blurred, new Size(3, 3), 0);

        return blurred;
    }

    public int verifyFace(File imageFile) {
        try {
            Mat inputFace = preprocessFace(detectFace(imageFile));
            if (inputFace == null) {
                System.out.println("❌ No face detected in input image");
                return -1;
            }

            if (userFaceSamples.isEmpty()) {
                System.out.println("❌ No registered faces in database");
                return -1;
            }

            double bestSimilarity = 0;
            int bestUserId = -1;
            double secondBestSimilarity = 0;
            int secondBestUserId = -1;

            // Compare with ALL samples of ALL users
            for (Map.Entry<Integer, List<Mat>> entry : userFaceSamples.entrySet()) {
                int userId = entry.getKey();
                List<Mat> userSamples = entry.getValue();

                double userBestSimilarity = 0;

                for (Mat storedFace : userSamples) {
                    double similarity = compareFaces(inputFace, storedFace);

                    if (similarity > userBestSimilarity) {
                        userBestSimilarity = similarity;
                    }
                }

                // Track best and second best
                if (userBestSimilarity > bestSimilarity) {
                    secondBestSimilarity = bestSimilarity;
                    secondBestUserId = bestUserId;
                    bestSimilarity = userBestSimilarity;
                    bestUserId = userId;
                } else if (userBestSimilarity > secondBestSimilarity) {
                    secondBestSimilarity = userBestSimilarity;
                    secondBestUserId = userId;
                }
            }

            // Calculate confidence (difference between best and second best)
            double confidence = bestSimilarity - secondBestSimilarity;

            System.out.println("Best match: " + bestSimilarity + "% for user " + bestUserId +
                    " (confidence: " + confidence + ")");

            // FIXED: More lenient acceptance criteria
            if (bestSimilarity > 60) {
                // Very high similarity - accept regardless of confidence
                System.out.println("✅✅ HIGH SIMILARITY MATCH: User " + bestUserId);
                return bestUserId;
            }
            else if (bestSimilarity > 50 && confidence > 3) {
                // Good similarity with reasonable confidence
                System.out.println("✅ GOOD MATCH: User " + bestUserId);
                return bestUserId;
            }
            else if (bestSimilarity > 45 && confidence > 5) {
                // Decent match with good confidence
                System.out.println("✅ ACCEPTABLE MATCH: User " + bestUserId);
                return bestUserId;
            }
            else {
                System.out.println("❌ No matching face found");
                return -1;
            }

        } catch (Exception e) {
            System.err.println("❌ Face verification error: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    private double compareFaces(Mat face1, Mat face2) {
        // 1. Template Matching
        Mat result = new Mat();
        Imgproc.matchTemplate(face1, face2, result, Imgproc.TM_CCOEFF_NORMED);
        Core.MinMaxLocResult mm = Core.minMaxLoc(result);
        double templateSimilarity = mm.maxVal * 100;

        // 2. Histogram comparison
        Mat hist1 = new Mat();
        Mat hist2 = new Mat();

        MatOfFloat ranges = new MatOfFloat(0, 256);
        MatOfInt histSize = new MatOfInt(256);

        Imgproc.calcHist(Arrays.asList(face1), new MatOfInt(0), new Mat(), hist1, histSize, ranges);
        Imgproc.calcHist(Arrays.asList(face2), new MatOfInt(0), new Mat(), hist2, histSize, ranges);

        Core.normalize(hist1, hist1, 0, 1, Core.NORM_MINMAX);
        Core.normalize(hist2, hist2, 0, 1, Core.NORM_MINMAX);

        double histogramSimilarity = Imgproc.compareHist(hist1, hist2, Imgproc.CV_COMP_CORREL) * 100;

        // 3. Pixel difference (MSE - Mean Squared Error)
        Mat diff = new Mat();
        Core.absdiff(face1, face2, diff);
        Scalar meanDiff = Core.mean(diff);
        double mse = meanDiff.val[0];
        double pixelSimilarity = Math.max(0, 100 - (mse * 100 / 255));

        // Weighted combination - give more weight to template matching
        return (templateSimilarity * 0.6) +
                (histogramSimilarity * 0.3) +
                (pixelSimilarity * 0.1);
    }

    public static File imageToFile(javafx.scene.image.Image fxImage) {
        try {
            BufferedImage bufferedImage = convertFxImageToBufferedImage(fxImage);
            File tempFile = File.createTempFile("face_", ".png");
            ImageIO.write(bufferedImage, "png", tempFile);
            tempFile.deleteOnExit();
            return tempFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static BufferedImage convertFxImageToBufferedImage(javafx.scene.image.Image fxImage) {
        int width = (int) fxImage.getWidth();
        int height = (int) fxImage.getHeight();

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        javafx.scene.image.PixelReader pixelReader = fxImage.getPixelReader();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                javafx.scene.paint.Color color = pixelReader.getColor(x, y);
                int argb = ((int) (color.getOpacity() * 255) << 24) |
                        ((int) (color.getRed() * 255) << 16) |
                        ((int) (color.getGreen() * 255) << 8) |
                        (int) (color.getBlue() * 255);
                bufferedImage.setRGB(x, y, argb);
            }
        }

        return bufferedImage;
    }

    public boolean hasUserFaces(int userId) {
        return userFaceSamples.containsKey(userId) && !userFaceSamples.get(userId).isEmpty();
    }

    public int getRegisteredUsersCount() {
        return userFaceSamples.size();
    }

    public int getUserSampleCount(int userId) {
        if (!userFaceSamples.containsKey(userId)) {
            return 0;
        }
        return userFaceSamples.get(userId).size();
    }
}