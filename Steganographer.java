import java.awt.image.*;
import javax.imageio.*;
import java.io.*;

public class Steganographer {
   public BufferedImage image; //the image that the Steganographer will encode and decode from
   
   public Steganographer(BufferedImage image) {
      this.image = image;
   }
   
   //encodes a 
   public void encode(File message) throws FileNotFoundException, IOException {
      FileInputStream input = new FileInputStream(message);
      
      //keeps track of pixels
      int x = 0;
      int y = 0;
      
      //endingCounter tracks the EoF sequence "0xff 0xff" that is printed at end of file
      //finished changes when the ending EoF is printed
      //write zeroes tracks if a "0xff" has been seen as file data,
      // and instructs the loop to print "0x00" next to avoid collisions with the EoF
      int endingCounter = 0;
      boolean finished = false;
      boolean writeZeroesNext = false;
      
      //inBuffer is the next message byte, imageBuffer is the 32-bit RGB value
      //the Rem values are the remaining two bit pairs that need to go from inBuffer to imageBuffer
      //for their respective bytes
      int inBuffer = input.read();
      int imageBuffer = image.getRGB(x, y);
      int inBufferRem = 4;
      int imageBufferRem = 3;
      
      //the least signifigant 2 bits of the Red, Green, and Blue bytes are changed to be bits of message data
      while (!finished) {
         //shifts bit pairs into last positions
         int inBufferShift = (inBufferRem * 2) - 2;
         int imageBufferShift = (imageBufferRem * 8) - 8;
         int bitsIn = (inBuffer >> inBufferShift) & 3;      //pulls out two relavent bits
         int subImage = (imageBuffer >> imageBufferShift) & 252; //pulls out all bits except last two for relavant byte
         int combinedByte = bitsIn | subImage;
         
         //"slots" the new 2 bits in with the original image data
         int imageWithSlot = imageBuffer & (~(0xff << imageBufferShift));
         imageBuffer = imageWithSlot | (combinedByte << imageBufferShift);
         inBufferRem--;
         imageBufferRem--;
         
         //checks special conditions and refreshes the inBuffer if empty
         if (inBufferRem == 0) {
            if (writeZeroesNext) {
               inBuffer = 0;
               writeZeroesNext = false;
            } else {
               inBuffer = input.read();
            }
            
            if (endingCounter == 2) {
               finished = true;
            } else if (endingCounter == 1) {
               inBuffer = 0xff;
               endingCounter++;
            } else if (inBuffer == -1) {
               endingCounter++;
               inBuffer = 0xff;
            } else if (inBuffer == 0xff) {
               writeZeroesNext = true;
            }           
            
            inBufferRem = 4;
         }
         //refreshes the imageBuffer when empty
         if (imageBufferRem == 0) {
            image.setRGB(x, y, imageBuffer);
            x++;
            if (x == image.getWidth()) {
               x = 0;
               y++;
            }
            if (y == image.getHeight()) {
               throw new IOException();
            }
            imageBuffer = image.getRGB(x, y);
            imageBufferRem = 3;
         }
      }
      image.setRGB(x, y, imageBuffer);
      
   }
   
   //decodes the image to an output File file.
   public File decode(File file) throws FileNotFoundException, IOException {
      FileOutputStream output = new FileOutputStream(file);
      
      //tracks the current pixel
      int x = 0;
      int y = 0;      
      
      //if previous byte was "0xff", check to see if the next byte is "0xff" or "0x00", to determine EoF
      boolean checkNextByte = false;
      
      //structure mirrors encode
      int outBuffer = 0;
      int imageBuffer = image.getRGB(x, y);
      int outBufferSize = 0;
      int imageBufferRem = 3;
      
      for (;;) {
         
         //flushes outBuffer and checks special conditions
         if (outBufferSize == 4) {
            if (checkNextByte) {
               if (outBuffer == 0xff) {
                  break;
               } else {
                  checkNextByte = false;
                  output.write(0xff);
               }
            } else if (outBuffer == 0xff) {
               checkNextByte = true;
            } else {
               output.write(outBuffer);
            }
            outBuffer = 0;
            outBufferSize = 0;
         }
         
         //builds the outBuffer from imageBuffer by grabbing 2 bits from the proper bytes in the 32-bit RGB data
         int imageBufferShift = (imageBufferRem * 8) - 8;
         int bitsOut = (imageBuffer >> imageBufferShift) & 3;
         
         int outBufferShift = 6 - (outBufferSize * 2);
         outBuffer = (bitsOut << outBufferShift) | outBuffer;
         
         outBufferSize++;
         imageBufferRem--;
         
         //refreshes the imageBuffer
         if (imageBufferRem == 0) {
            x++;
            if (x == image.getWidth()) {
               x = 0;
               y++;
            }
            if (y == image.getHeight()) {
               throw new IOException();
            }
            imageBuffer = image.getRGB(x, y);
            imageBufferRem = 3;
         }
      }
      return file;
   }
}