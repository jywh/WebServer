package webServer;

public class ErrorResponse {

	private int responseCode;
	private String responsePhrase;
	private String fileName;
	
	public ErrorResponse(int responseCode, String responsePhrase, String fileName){
		this.responseCode = responseCode;
		this.responsePhrase = responsePhrase;
		this.fileName = fileName;
	}
	
	public int getResponseCode(){
		return responseCode;
	}

	public String getResponsePhrase(){
		return responsePhrase;
	}
	
	public String getFilePath(){
		return fileName;
	}
	
}
