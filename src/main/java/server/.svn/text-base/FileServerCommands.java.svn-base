package server;

import message.response.*;
import java.io.IOException;
import cli.Command;

public class FileServerCommands implements IFileServerCli{
  private FileServerLogic logic;

  public FileServerCommands(FileServerLogic l){
    this.logic = l;
  }
  
  @Command
  public MessageResponse exit() throws IOException{
    logic.shutdown();
    System.in.close();
    return new MessageResponse("Shutting down (FS Shell)");
  }
}