package net.zergrush;

public class InfoScreenRequest {

    private final String pageName;
    private final Object pageData;

    public InfoScreenRequest(String pageName, Object pageData) {
        this.pageName = pageName;
        this.pageData = pageData;
    }
    public InfoScreenRequest(String pageName) {
        this(pageName, null);
    }

    public String getPageName() {
        return pageName;
    }

    public Object getPageData() {
        return pageData;
    }

    public InfoScreenRequest onDone(String newPageName, Object result) {
        return new InfoScreenRequest(newPageName);
    }

}
