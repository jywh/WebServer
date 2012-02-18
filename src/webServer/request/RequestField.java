package webServer.request;

public class RequestField {

	private String field;
	private String content;
	
	public RequestField(String field, String content){
		this.field = field;
		this.content = content;
	}
	
	public String getField(){
		return field;
	}
	
	public String getContent(){
		return content;
	}
	
}
