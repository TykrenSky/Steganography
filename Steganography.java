import java.awt.image.*;
import javax.imageio.*;
import java.io.*;
import java.util.*;

public class Steganography {

   public static void main(String[] args) throws IOException {
      Scanner console = new Scanner(System.in);
      String command = "";
      System.out.println("Hi there! Welcome to a basic Steganography kit developed by Tieran Rashid");
      System.out.println("You can (e)ncode or (d)ecode files, and provide places to save the results!");
      System.out.println("you can also type \"exit\" to exit the program, or \"?\" to see a list of commands");
      
      while (!command.equals("exit")) {
         System.out.print(">");
         command = console.nextLine().toLowerCase();
         
         if (command.equals("e")) {
            System.out.print("Message to encode? (text file) ");
            String messageName = console.nextLine();
            System.out.print("File name of input image? ");
            String inputImageName = console.nextLine();
            System.out.print("File name of output image? (any name, must end in .png) ");
            String outputImageName = console.nextLine();
            
            File message = new File(messageName);
            BufferedImage input = ImageIO.read(new File(inputImageName));
            Steganographer sten = new Steganographer(input);
            System.out.println("Encoding...");
            sten.encode(message);
            ImageIO.write(input, "png", new File(outputImageName));
            System.out.println("Complete!");
         } else if (command.equals("d")) {
            System.out.print("Input image? (warning, only use steganographic images to avoid error) ");
            String inputImageName = console.nextLine();
            System.out.print("Output text location? ");
            String outputFileName = console.nextLine();
            
            File outputFile = new File(outputFileName);
            BufferedImage input = ImageIO.read(new File(inputImageName));
            Steganographer sten = new Steganographer(input);
            System.out.println("Decoding...");
            sten.decode(outputFile);
            System.out.println("Complete!");
         } else if (command.equals("?")) {
            System.out.println("Type \"e\" to encode a file, \"d\" to decode, and \"exit\" to quit!");
         }
      }
   }
}