package sk.mimac.perun.server.model;

/**
 *
 * @author Mimac
 */
public class ImagesModel {
    
    private String name;
    
    private int count;
    
    private long mostRecentTimestamp;
    
    private String mostRecentFileName;
    
    private long mostRecentSize;

    public ImagesModel(String name, int count, long mostRecentTimestamp, String mostRecentFileName, long mostRecentSize) {
        this.name = name;
        this.count = count;
        this.mostRecentTimestamp = mostRecentTimestamp;
        this.mostRecentFileName = mostRecentFileName;
        this.mostRecentSize = mostRecentSize;
    } 
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getMostRecentTimestamp() {
        return mostRecentTimestamp;
    }

    public void setMostRecentTimestamp(long mostRecentTimestamp) {
        this.mostRecentTimestamp = mostRecentTimestamp;
    }

    public String getMostRecentFileName() {
        return mostRecentFileName;
    }

    public void setMostRecentFileName(String mostRecentFileName) {
        this.mostRecentFileName = mostRecentFileName;
    }

    public long getMostRecentSize() {
        return mostRecentSize;
    }

    public void setMostRecentSize(long mostRecentSize) {
        this.mostRecentSize = mostRecentSize;
    }

}
