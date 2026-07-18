package com.videoshare.common.search;

import java.util.List;

public class SearchResult {
    private List<String> videoIds;
    private long total;

    public SearchResult(List<String> videoIds, long total) {
        this.videoIds = videoIds;
        this.total = total;
    }

    public List<String> getVideoIds() { return videoIds; }
    public long getTotal() { return total; }
}
