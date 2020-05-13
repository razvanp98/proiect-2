import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class KMeans {
    private ArrayList<Cluster> clusters;
    private int iterations = 0;

    public KMeans() {
        this.clusters = new ArrayList<>();
    }

    public BufferedImage perform(BufferedImage img, int k) {
        BufferedImage result = null;

        boolean movedCluster = true;
        int width = img.getWidth();
        int height = img.getHeight();
        int[] LUT = new int[width * height]; // Look-up Table pentru a stoca ID-urile clusterilor

        Arrays.fill(LUT, -1);
        this.clusters = generateClusters(img, k);

        while (movedCluster) {
            movedCluster = false;
            
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {

                    int color = img.getRGB(i, j);
                    Cluster cluster = this.findMinimal(color); // Cauta clusterul cu cea mai mica distanta fata de pixel

                    // Daca ID-ul clusterului cu distanta cea mai mica nu se afla in LUT, sterge pixelul din cluster la pozitia width * j + i
                    if (LUT[width * j + i] != cluster.getUid()) {
                        // Se afla deja un cluster la aceasta pozitie, sterge culoarea
                        if (LUT[width * j + i] != -1) {
                            clusters.get(LUT[width * j + i]).removeColor(color);
                        }

                        cluster.addColor(color);
                        LUT[width * j + i] = cluster.getUid();

                        movedCluster = true; // Continua bucla pana nu se mai observa nicio modificare
                    }
                    this.iterations++;
                }
            }
        }

        result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Scrie pixelii pe imagine
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                int id = LUT[width * j + i];
                result.setRGB(i, j, clusters.get(id).getRGB());
            }
        }
        return result;
    }

    // Genereaza clustere cu pas random => RANGE [0 : (width - 1) sau (height - 1)]
    public ArrayList<Cluster> generateClusters(BufferedImage img, int k) {
        ArrayList<Cluster> result = new ArrayList<>();
        Random rand = new Random();

        int x = 0, y = 0;

        for (int i = 0; i < k; i++) {
            if(x == img.getWidth() - 1 || y == img.getHeight() - 1) {
                x = 0;
                y = 0;
            }

            int stepX = rand.nextInt(img.getWidth() - 1) / k;
            int stepY = rand.nextInt(img.getHeight() - 1) / k;

            x += stepX;
            y += stepY;

            Cluster cNew = new Cluster(i, img.getRGB(x, y));
            result.add(cNew);
        }
        return result;
    }

    // Cauta cluster-ul cu distanta minima fata de pixel
    public Cluster findMinimal(int color) {
        Cluster cluster = null;
        double minim = Integer.MAX_VALUE;

        for (Cluster it : this.clusters) {
            double dist = it.getDistance(color);
            if (dist < minim) {
                minim = dist;
                cluster = it;
            }
        }
        return cluster;
    }

    public void save(BufferedImage img, String path, String ext) {
        File file = new File(path);
        try {
            ImageIO.write(img, ext, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BufferedImage load(String path) {
        BufferedImage result = null;
        try {
            result = ImageIO.read(new File(path));
        } catch (Exception e) {
           e.printStackTrace();
        }

        return result;
    }

    public int getIterations() {
        return this.iterations;
    }

    public int resetIterations() {
       return this.iterations = 0;
    }
}