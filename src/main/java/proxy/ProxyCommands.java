package proxy;

import proxy.IProxyCli;
import cli.*;
import message.response.MessageResponse;
import message.Response;
import java.io.IOException;
import proxy.Statistics;
import proxy.ProxyLogic;

public class ProxyCommands implements IProxyCli{
  private Statistics stats;
  private ProxyLogic logic;

  public ProxyCommands(ProxyLogic l){
    this.logic = l;
    this.stats = l.getStats();
  }

  @Command
  public Response fileservers() throws IOException{
    return new MessageResponse(stats.fileserverToString());
  }
  @Command
  public Response users() throws IOException{
    return new MessageResponse(stats.usersToString());
  }
  @Command
  public MessageResponse exit() throws IOException{
    logic.shutdown();
    return new MessageResponse("Shutting down (Shell)");
  }
}