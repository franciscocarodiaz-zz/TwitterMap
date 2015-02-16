package fcd.com.twittermap.Twitter;

public class Authenticated {
	public String token_type;
    public String access_token;

    public Authenticated(String token_type, String access_token) {
        this.token_type = token_type;
        this.access_token = access_token;
    }
}
