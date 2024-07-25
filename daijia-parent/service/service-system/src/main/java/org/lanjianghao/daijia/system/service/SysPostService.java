package org.lanjianghao.daijia.system.service;

import org.lanjianghao.daijia.model.entity.system.SysPost;
import org.lanjianghao.daijia.model.query.system.SysPostQuery;
import org.lanjianghao.daijia.model.vo.base.PageVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface SysPostService extends IService<SysPost> {

    PageVo<SysPost> findPage(Page<SysPost> pageParam, SysPostQuery sysPostQuery);

    void updateStatus(Long id, Integer status);

    List<SysPost> findAll();
}
