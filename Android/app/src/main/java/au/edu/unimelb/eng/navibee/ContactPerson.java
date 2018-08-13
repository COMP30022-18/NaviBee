package au.edu.unimelb.eng.navibee;

public class ContactPerson {
    private String url;
    private String name;

    public ContactPerson(String name, String url) {
        this.url = url;
        this.name = name;
    }

    public String getUrl() {
        return this.url;
    }

    public String getName() {
        return this.name;
    }

    public void setUrl(String url){
        this.url = url;
    }
    public void setName(String name){
        this.name = name;
    }
}
