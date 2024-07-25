package org.lanjianghao.daijia.mgr.service.impl;

import org.lanjianghao.daijia.mgr.service.SysMenuService;
import org.lanjianghao.daijia.model.entity.system.SysMenu;
import org.lanjianghao.daijia.model.vo.system.AssginMenuVo;
import org.lanjianghao.daijia.system.client.SysMenuFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysMenuServiceImpl implements SysMenuService {

    @Autowired
    private SysMenuFeignClient sysMenuFeignClient;

    @Override
    public void save(SysMenu sysMenu) {
        sysMenuFeignClient.save(sysMenu);
    }

    @Override
    public void update(SysMenu sysMenu) {
        sysMenuFeignClient.update(sysMenu);
    }

    @Override
    public void remove(Long id) {
        sysMenuFeignClient.remove(id);
    }

    @Override
    public List<SysMenu> findNodes() {
        return sysMenuFeignClient.findNodes().getData();
    }

    @Override
    public void doAssign(AssginMenuVo assginMenuVo) {
        sysMenuFeignClient.doAssign(assginMenuVo);
    }

    @Override
    public List<SysMenu> toAssign(Long roleId) {
        return sysMenuFeignClient.toAssign(roleId).getData();
    }
}
