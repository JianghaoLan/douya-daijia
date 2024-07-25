package org.lanjianghao.daijia.mgr.service;


import org.lanjianghao.daijia.model.entity.system.SysUser;
import org.lanjianghao.daijia.model.query.system.SysUserQuery;
import org.lanjianghao.daijia.model.vo.base.PageVo;

public interface SysUserService {

    SysUser getById(Long id);

    void save(SysUser sysUser);

    void update(SysUser sysUser);

    void remove(Long id);

    PageVo<SysUser> findPage(Long page, Long limit, SysUserQuery sysUserQuery);

    void updateStatus(Long id, Integer status);


}
