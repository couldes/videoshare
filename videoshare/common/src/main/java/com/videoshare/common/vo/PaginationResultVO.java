
package com.videoshare.common.vo;

import java.util.List;

public class PaginationResultVO<T> {

    private Integer total;    // 总记录数
    private Integer pageSize; // 每页条数
    private Integer pageNum;  // 当前页码（从 1 开始）
    private Integer pages;    // 总页数（= ceil(total / pageSize)）
    private List<T> list;     // 当前页的数据列表

    //  全参构造，方便 Service 层快速创建 
    public PaginationResultVO(Integer total, Integer pageSize, Integer pageNum, List<T> list) {
        this.total    = total;
        this.pageSize = pageSize;
        this.pageNum  = pageNum;
        this.list     = list;
        // 自动计算总页数：Math.ceil(100 / 15) = 7 页
        this.pages    = (int) Math.ceil((double) total / pageSize);
    }

    //  Getters & Setters 
    public Integer getTotal()    { return total; }
    public void setTotal(Integer total) { this.total = total; }

    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }

    public Integer getPageNum()  { return pageNum; }
    public void setPageNum(Integer pageNum) { this.pageNum = pageNum; }

    public Integer getPages()    { return pages; }
    public void setPages(Integer pages) { this.pages = pages; }

    public List<T> getList()     { return list; }
    public void setList(List<T> list) { this.list = list; }
}
