import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Training {

	// Referenced this page for file IO and processing files:
	// https://stackoverflow.com/questions/17279049/reading-a-idx-file-type-in-java

	public static void main(String[] args) {

		// double[][] inputs = {
		// {0,0},
		// {0,1},
		// {1,0},
		// {1,1}
		// };
		// double[][] desiredOutput = {{0},{1},{1},{0}};

		FileInputStream inImage = null;
		FileInputStream inLabel = null;

		String inputImagePath = "t10k-images.idx3-ubyte";
		String inputLabelPath = "t10k-labels.idx1-ubyte";

		try {
			// LABELS
			inLabel = new FileInputStream(inputLabelPath);
			int magicNumberLabels = (inLabel.read() << 24) | (inLabel.read() << 16) | (inLabel.read() << 8) | (inLabel.read());
			int numberOfLabels = (inLabel.read() << 24) | (inLabel.read() << 16) | (inLabel.read() << 8) | (inLabel.read());
			double[][] desiredOutput = new double[numberOfLabels][1];

			for (int i = 0; i < numberOfLabels; i++) {
				desiredOutput[i][0] = inLabel.read();

				if (i < 10) // verify labels imported
					System.out.println(desiredOutput[i][0]);
			}

			// IMAGES
			inImage = new FileInputStream(inputImagePath);
			int magicNumberImages = (inImage.read() << 24) | (inImage.read() << 16) | (inImage.read() << 8) | (inImage.read());
			int numberOfImages = (inImage.read() << 24) | (inImage.read() << 16) | (inImage.read() << 8) | (inImage.read());
			int numberOfRows = (inImage.read() << 24) | (inImage.read() << 16) | (inImage.read() << 8) | (inImage.read());
			int numberOfColumns = (inImage.read() << 24) | (inImage.read() << 16) | (inImage.read() << 8) | (inImage.read());

			int numberOfPixels = numberOfRows * numberOfColumns;
			double[][] inputs = new double[numberOfImages][numberOfPixels];

			for (int i = 0; i < numberOfImages; i++) {
				double[] imgPixels = new double[numberOfPixels];

				for (int p = 0; p < numberOfPixels; p++) {
					imgPixels[p] = inImage.read();
				}

				inputs[i] = imgPixels;
				if (i < 10)
					System.out.println(imgPixels.toString());
			}

			FeedForwardNetwork n = new FeedForwardNetwork(numberOfPixels, 1, numberOfPixels/2, 10);
			
			n.initNetwork(inputs, desiredOutput, 0.3, 0);
			n.trainNetwork(10000, false);
			n.printWeights();
			n.testNetwork();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inImage != null) {
				try {
					inImage.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (inLabel != null) {
				try {
					inLabel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
}