package org.lanjianghao.daijia.mgr.service.impl;

import org.lanjianghao.daijia.mgr.service.SysPostService;
import org.lanjianghao.daijia.model.entity.system.SysPost;
import org.lanjianghao.daijia.model.query.system.SysPostQuery;
import org.lanjianghao.daijia.model.vo.base.PageVo;
import org.lanjianghao.daijia.system.client.SysPostFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class SysPostServiceImpl implements SysPostService {

    @Autowired
    private SysPostFeignClient sysPostFeignClient;

    @Override
    public SysPost getById(Long id) {
        return sysPostFeignClient.getById(id).getData();
    }

    @Override
    public void save(SysPost sysPost) {
        sysPostFeignClient.save(sysPost);
    }

    @Override
    public void update(SysPost sysPost) {
        sysPostFeignClient.update(sysPost);
    }

    @Override
    public void remove(Long id) {
        sysPostFeignClient.remove(id);
    }

    @Override
    public PageVo<SysPost> findPage(Long page, Long limit, SysPostQuery sysPostQuery) {
        return sysPostFeignClient.findPage(page, limit, sysPostQuery).getData();
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        sysPostFeignClient.updateStatus(id, status);
    }

    @Override
    public List<SysPost> findAll() {
        return sysPostFeignClient.findAll().getData();
    }
}
