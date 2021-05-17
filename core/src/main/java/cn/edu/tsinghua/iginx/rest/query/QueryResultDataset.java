package cn.edu.tsinghua.iginx.rest.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class QueryResultDataset
{
    private int size = 0;
    private int sampleSize = 0;
    private List<Long> timestamps = new ArrayList<>();
    private List<Object> values = new ArrayList<>();
    private List<String> paths = new ArrayList<>();

    public List<String> getPaths()
    {
        return paths;
    }

    public void setPaths(List<String> paths)
    {
        this.paths = paths;
    }

    public void addPath(String path)
    {
        paths.add(path);
    }

    public int getSize()
    {
        return size;
    }

    public List<Long> getTimestamps()
    {
        return timestamps;
    }

    public List<Object> getValues()
    {
        return values;
    }

    public void setSize(int size)
    {
        this.size = size;
    }

    public void setTimestamps(List<Long> timestamps)
    {
        this.timestamps = timestamps;
    }

    public void setValues(List<Object> values)
    {
        this.values = values;
    }

    private void addSize()
    {
        this.size ++;
    }

    private void addTimestamp(long timestamp)
    {
        this.timestamps.add(timestamp);
    }

    private void addValue(Object value)
    {
        this.values.add(value);
    }

    public void add(long timestamp, Object value)
    {
        addTimestamp(timestamp);
        addValue(value);
        addSize();
    }

    public void setSampleSize(int sampleSize)
    {
        this.sampleSize = sampleSize;
    }

    public int getSampleSize()
    {
        return sampleSize;
    }
}