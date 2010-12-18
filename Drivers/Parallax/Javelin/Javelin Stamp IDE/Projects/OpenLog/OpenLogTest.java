import stamp.core.*;
import stamp.peripheral.memory.openlog.*;
/**
 * SparkFun OpenLog test program
 *
 * @version 1.0 April, 2010
 * @author Paul Ring - hexor@coolbox.se
 */

public class OpenLogTest {

  // **************************************************************** //
  //                  Pin definitions
  // **************************************************************** //
  public static final int OpenLog_RXI_PIN = CPU.pin6;
  public static final int OpenLog_TXO_PIN = CPU.pin7;
  public static final int OpenLog_RESET_PIN = CPU.pin14;
  // **************************************************************** //


  public static void main() {

    // Reset openlog
    CPU.writePin(OpenLog_RESET_PIN, true);  // OpenLog reset pin should be high at all times

    Timer timer = new Timer();
    Uart rxUart = new Uart(Uart.dirReceive,
                           OpenLog_TXO_PIN,
                           Uart.dontInvert,
                           Uart.speed19200,
                           Uart.stop1);

    Uart txUart = new Uart(Uart.dirTransmit,
                           OpenLog_RXI_PIN,
                           Uart.dontInvert,
                           Uart.speed19200,
                           Uart.stop1);

    StringBuffer bufferTemp = new StringBuffer(300);
    OpenLog openLog = new OpenLog(txUart,
                                  rxUart,
                                  OpenLog_RESET_PIN,
                                  new StringBuffer(100),
                                  new Timer());
    // Restart the card
    if (!openLog.openLogRestart()) {
      System.out.println("*** Error: OpenLog is not responding *** ");
      System.out.println("Stopping...");
      while (true) {CPU.delay(10);}
    }

    // Initialize the card

    if (!openLog.openLogInit()) {
      System.out.println("*** Error: OpenLog cannot be initialized *** ");
      System.out.println("Stopping...");
      while (true) {CPU.delay(10);}
    }


    StringBuffer fileData = new StringBuffer(500);
    System.out.println("*** OpenLog is now initialized *** ");
    System.out.println(" ");

    // Create folder
    changeFolder("misc", openLog);
    // Create folder
    changeFolder("test", openLog);
    System.out.print(0x10);

    // *************************************************** //
    // Create file openlog.txt if it does not already exist
    // *************************************************** //
    if (openLog.fileSize("openlog.txt") == null) {
      System.out.println(" ");
      System.out.println("Creating file \"openlog.txt\"");
      fileData.clear();
      fileData.append("OpenLog is a simple serial logger based on the ATmega328 running at 16MHz. The ATmega328 is able to talk to high capacity (larger than 2GB) SD cards (but requires a recompile and reflash). The whole purpose of this logger was to create a logger that just powered up and worked. OpenLog ships with the standard (stk500v1) serial bootloader so you can load new firmware with a simple serial connection. I just want it to work : Go to the product page and buy OpenLog. We pre-program the device with latest firmware. I just want to use it : Just power up OpenLog and by default (in v1.1 and above) OpenLog will log any serial text thrown at it. No commands, no configuration required. Just power up and go!");
      //fileData.append("This is a test #1");

      fileData.append("\r\n");
    }

    fileData.append("This is a test #1\r\n");
    if (!openLog.writeFile("openlog.txt", fileData, true)) {
      System.out.println("Could not write to file \"openlog.txt\"");
    } else {
      System.out.println("Success: \"openlog.txt\"");
    }


    // *************************************************** //
    // Create file openlog1.txt if it does not already exist
    // *************************************************** //
    if (openLog.fileSize("openlog1.txt") == null) {
      System.out.println(" ");
      System.out.println("Creating file \"openlog1.txt\"");
      fileData.clear();
      fileData.append("Just some content");
      fileData.append("\r\n");

      if (!openLog.writeFile("openlog1.txt", fileData, true)) {
        System.out.println("Could not write to file \"openlog1.txt\"");
      } else {
        System.out.println("Success: \"openlog1.txt\"");
      }
    }


    // List all the files in the directory
    listFiles(openLog, bufferTemp);

    // *************************************************** //
    // Reading openlog.txt
    // *************************************************** //
    System.out.println(" ");
    System.out.println("Reading file \"openlog.txt\"");
    {
      if (openLog.openFile("openlog.txt")) {
        openLog.setFilePosition(0);
        while (openLog.readFile(bufferTemp, 80)) {
            System.out.print(bufferTemp.toString());
        }
        openLog.closeFile();
      }
    }

    // *************************************************** //
    // Removing openlog.txt
    // *************************************************** //
    if (openLog.deleteFile("openlog.txt")) {
      System.out.println("\"openlog.txt\" removed");
    } else {
      System.out.println("Could not remove\"openlog.txt\"");
    }

    // List all the files in the directory
    listFiles(openLog, bufferTemp);

    // *************************************************** //
    // Changing directory
    // *************************************************** //
    if (openLog.prevDir()) {
      System.out.println("\"cd ..\" success");
    } else {
      System.out.println("\"cd ..\" error");
    }

    // List all the files in the directory
    listFiles(openLog, bufferTemp);

    // *************************************************** //
    // Removing all files
    // *************************************************** //
    if (openLog.deleteFile("*")) {
      System.out.println("All files (*) has been removed");
    } else {
      System.out.println("Could not remove files (*)");
    }

    // List all the files in the directory
    listFiles(openLog, bufferTemp);

    if (openLog.deleteFile("*.*")) {
      System.out.println("All files (*.*) has been removed");
    } else {
      System.out.println("Could not remove files (*.*)");
    }

    // *************************************************** //
    // Changing directory
    // *************************************************** //
    if (openLog.prevDir()) {
      System.out.println("\"cd ..\" success");
    } else {
      System.out.println("\"cd ..\" error");
    }

    // List all the files in the directory
    listFiles(openLog, bufferTemp);

    while (true) {CPU.delay(10);}
  }

  public static void changeFolder(String folderName, OpenLog openLog) {
    if (!openLog.changeDir(folderName)) {
      System.out.println("Creating folder...");
      if (openLog.createDir(folderName)) {
        System.out.print("Folder \"");
        System.out.print(folderName);
        System.out.print("\" created...");
      } else {
        System.out.print("Error creating folder \"");
        System.out.print(folderName);
        System.out.print("\"");
      }

      if (!openLog.changeDir(folderName)) {
        System.out.print("Cannot change to folder \"");
        System.out.print(folderName);
        System.out.println("\"");
      }
    } else {
      System.out.print("Changed to folder \"");
        System.out.print(folderName);
        System.out.println("\"");
    }
  }

  public static void listFiles(OpenLog openLog, StringBuffer buffer) {
    System.out.println("Listing files...");
    buffer.clear();
    if (openLog.listDirectoryStart()) {
      buffer.append(openLog.listDirectoryCount());
      buffer.append(" file(s)");
      System.out.println(buffer.toString());
      OpenLog.FileInfo fileInfo;
      while((fileInfo = openLog.listDirectoryNextEntry()) != null) {
        System.out.print("File: ");
        System.out.print(fileInfo.fileName.toString());
        System.out.print(" ");
        System.out.println(fileInfo.fileSize.utoString());
      }
      openLog.listDirectoryEnd();
    }
  }
}