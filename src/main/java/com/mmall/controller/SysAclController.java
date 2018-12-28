package com.mmall.controller;

import com.mmall.beans.PageQuery;
import com.mmall.common.JsonData;
import com.mmall.param.AclModuleParam;
import com.mmall.service.SysAclService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping("/sys/acl")
@Slf4j
public class SysAclController {
    @Resource
    private SysAclService sysAclService;

    @RequestMapping("/save.json")
    @ResponseBody
    public JsonData saveAclModule(AclModuleParam param){
        return JsonData.success();
    }

    @RequestMapping("/update.json")
    @ResponseBody
    public JsonData updateAclModule(AclModuleParam param){
        return JsonData.success();
    }

    @RequestMapping("/list.json")
    @ResponseBody
    public JsonData list(@Param("aclModuleId") Integer aclModuleId, PageQuery pageQuery){

    }
}
