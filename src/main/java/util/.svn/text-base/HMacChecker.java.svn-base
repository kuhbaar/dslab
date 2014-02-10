package util;

import java.io.IOException;
import java.security.MessageDigest;

import javax.crypto.Mac;

import message.Request;
import message.Response;
import message.request.HMacRequest;
import message.response.HMacResponse;

import org.bouncycastle.util.encoders.Base64;

public class HMacChecker {
	
	public static HMacRequest getSignedRequest(Mac hMac, Request req)	{
    	try {
			hMac.update(Serializer.serialize(req));
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
   	  	byte[] computedHash = hMac.doFinal();
   	  	computedHash = Base64.encode(computedHash);
   	  	HMacRequest hReq = new HMacRequest(req, computedHash);
    	return hReq;
    }
	
	public static HMacResponse getSignedResponse(Mac hMac, Response resp)	{
    	try {
			hMac.update(Serializer.serialize(resp));
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
   	  	byte[] computedHash = hMac.doFinal();
   	  	computedHash = Base64.encode(computedHash);
    	HMacResponse hResponse = new HMacResponse(resp, computedHash);
    	return hResponse;
    }
	
	
	public static boolean verifySignedMessage(Mac hMac, HMacResponse hResp)	{
		//Check HMAC
	  	try {
			hMac.update(Serializer.serialize(hResp.getResponse()));
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] computedHash = hMac.doFinal();
		computedHash = Base64.encode(computedHash);
		boolean validHash = MessageDigest.isEqual(computedHash,hResp.getHMac());
		return validHash;
	}
}
