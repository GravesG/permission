package com.mmall.service;

import com.mmall.dao.SysDeptMapper;
import com.mmall.exception.ParamException;
import com.mmall.model.SysDept;
import com.mmall.param.DeptParam;
import com.mmall.util.BeanValidator;
import com.mmall.util.LevelUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class SysDeptService {

    @Resource
    private SysDeptMapper sysDeptMapper;

    public void save (DeptParam param){
        BeanValidator.check(param);
        if(checkExist(param.getParentId(),param.getName(),param.getId())){
            throw new ParamException("同一层级下存在相同名称的部门");
        }
        //构造SysDept对象
        SysDept dept = SysDept.builder().parentId(param.getParentId()).name(param.getName())
                .seq(param.getSeq()).remark(param.getRemake()).build();
        //set层级
        dept.setLevel(LevelUtil.calculateLevel(getLevel(param.getParentId()),param.getId()));
        dept.setOperator("system");//TODO
        dept.setOperateIp("");
    }

    //判断部门是否在同一层级下
    public boolean checkExist(Integer parentId, String deptName, Integer deptId){
        //TODO
        return true;
    }

    public String getLevel(Integer deptId){
        SysDept dept = sysDeptMapper.selectByPrimaryKey(deptId);
        if(dept == null){
            return null;
        }
        return dept.getLevel();
    }
}
