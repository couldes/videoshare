
package com.videoshare.common.vo;

import java.util.List;

/**
 * 通用分页结果包装类
 *
 * 所有需要分页的接口都返回这个格式：
 * {
 *   "total":    100,          // 总记录数（用于前端计算总页数）
 *   "pageSize": 15,           // 每页条数
 *   "pageNum":  1,            // 当前页码
 *   "list":     [ {...}, ... ] // 当前页数据
 * }
 *
 * 泛型 <T> 表示 list 里装什么类型都行
 * 例：PaginationResultVO<UserInfoVO>  → list 里是用户列表
 *     PaginationResultVO<VideoVO>     → list 里是视频列表
 *
 * 类比：图书馆借书记录单
 *   total    = 馆内总书数
 *   pageSize = 一次借出几本
 *   pageNum  = 第几批
 *   list     = 这批书的列表
 */
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
