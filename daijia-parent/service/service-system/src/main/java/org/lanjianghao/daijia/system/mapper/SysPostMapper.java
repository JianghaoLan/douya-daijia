package org.lanjianghao.daijia.system.mapper;

import org.lanjianghao.daijia.model.entity.system.SysPost;
import org.lanjianghao.daijia.model.query.system.SysPostQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface SysPostMapper extends BaseMapper<SysPost> {

    IPage<SysPost> selectPage(Page<SysPost> page, @Param("query") SysPostQuery sysPostQuery);

}
