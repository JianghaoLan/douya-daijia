package org.lanjianghao.daijia.system.client;

import org.lanjianghao.daijia.common.result.Result;
import org.lanjianghao.daijia.model.entity.system.SysPost;
import org.lanjianghao.daijia.model.query.system.SysPostQuery;
import org.lanjianghao.daijia.model.vo.base.PageVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(value = "service-system")
public interface SysPostFeignClient {

    @PostMapping("/sysPost/findPage/{page}/{limit}")
    Result<PageVo<SysPost>> findPage(
            @PathVariable("page") Long page,
            @PathVariable("limit") Long limit,
            @RequestBody SysPostQuery sysPostQuery);

    @GetMapping("/sysPost/getById/{id}")
    Result<SysPost> getById(@PathVariable Long id);

    @GetMapping("/sysPost/findAll")
    Result<List<SysPost>> findAll();

    @PostMapping("/sysPost/save")
    Result<Boolean> save(@RequestBody SysPost sysPost);

    @PutMapping("/sysPost/update")
    Result<Boolean> update(@RequestBody SysPost sysPost);

    @DeleteMapping("/sysPost/remove/{id}")
    Result<Boolean> remove(@PathVariable("id") Long id);

    @GetMapping("/sysPost/updateStatus/{id}/{status}")
    Result<Boolean> updateStatus(@PathVariable("id") Long id, @PathVariable("status") Integer status);

}

