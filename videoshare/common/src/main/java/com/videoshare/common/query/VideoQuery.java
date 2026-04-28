// 路径: common/src/main/java/com/videoshare/query/VideoQuery.java
package com.videoshare.common.query;

/**
 * 视频列表查询条件
 * 前端 GET /video/list?pageNum=1&pageSize=20&category=tech&keyword=vue
 */
public class VideoQuery {

    private Integer pageNum  = 1;
    private Integer pageSize = 20;
    private String  keyword;    // 标题关键词（模糊搜索）
    private String  category;  // 分类过滤
    private String  userId;    // 指定用户的视频（个人主页用）
    private Integer status;    // 默认只查 status=1（已发布）
    private String  orderBy;   // 排序：create_time / view_count / like_count

    /** SQL OFFSET = (pageNum - 1) * pageSize */
    public Integer getOffset() {
        return (this.pageNum - 1) * this.pageSize;
    }

    public Integer getPageNum()  { return pageNum; }
    public void setPageNum(Integer v) {
        this.pageNum = (v == null || v < 1) ? 1 : v;
    }
    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer v) {
        if (v == null || v < 1)  this.pageSize = 20;
        else if (v > 100)        this.pageSize = 100;
        else                     this.pageSize = v;
    }
    public String  getKeyword()  { return keyword; }
    public void setKeyword(String v) {
        this.keyword = (v != null && v.trim().isEmpty()) ? null : v;
    }
    public String  getCategory() { return category; }
    public void    setCategory(String v) { this.category = v; }
    public String  getUserId()   { return userId; }
    public void    setUserId(String v)   { this.userId = v; }
    public Integer getStatus()   { return status; }
    public void    setStatus(Integer v)  { this.status = v; }
    public String  getOrderBy()  { return orderBy; }
    public void    setOrderBy(String v)  { this.orderBy = v; }
}