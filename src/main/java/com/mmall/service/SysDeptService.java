package com.mmall.service;

import com.google.common.base.Preconditions;
import com.mmall.common.RequestHolder;
import com.mmall.dao.SysDeptMapper;
import com.mmall.dao.SysUserMapper;
import com.mmall.exception.ParamException;
import com.mmall.model.SysDept;
import com.mmall.param.DeptParam;
import com.mmall.util.BeanValidator;
import com.mmall.util.IpUtil;
import com.mmall.util.LevelUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class SysDeptService {

    @Resource
    private SysDeptMapper sysDeptMapper;
    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private SysLogService sysLogService;

    public void save (DeptParam param){
        BeanValidator.check(param);
        if(checkExist(param.getParentId(),param.getName(),param.getId())){
            throw new ParamException("同一层级下存在相同名称的部门");
        }
        //构造SysDept对象
        SysDept dept = SysDept.builder().parentId(param.getParentId()).name(param.getName())
                .seq(param.getSeq()).remark(param.getRemark()).build();
        //set层级
        dept.setLevel(LevelUtil.calculateLevel(getLevel(param.getParentId()),param.getParentId()));
        dept.setOperator(RequestHolder.getCurrentUser().getUsername());
        dept.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        dept.setOperateTime(new Date());
        sysDeptMapper.insertSelective(dept);
        sysLogService.saveDeptLog(null, dept);
    }

    public void update(DeptParam param){
        BeanValidator.check(param);
        if(checkExist(param.getParentId(),param.getName(),param.getId())){
            throw new ParamException("同一层级下存在相同名称的部门");
        }

        SysDept before = sysDeptMapper.selectByPrimaryKey(param.getId());
        Preconditions.checkNotNull(before,"待更新的部门不存在");
        if(checkExist(param.getParentId(),param.getName(),param.getId())){
            throw new ParamException("同一层级下存在相同名称的部门");
        }
        SysDept after = SysDept.builder().id(param.getId()).parentId(param.getParentId()).name(param.getName())
                .seq(param.getSeq()).remark(param.getRemark()).build();
        after.setLevel(LevelUtil.calculateLevel(getLevel(param.getParentId()), param.getParentId()));
        after.setOperator(RequestHolder.getCurrentUser().getUsername());
        after.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        after.setOperateTime(new Date());
        updateWithChild(before,after);
        sysLogService.saveDeptLog(before, after);
    }

    @Transactional
    public void updateWithChild(SysDept before, SysDept after){
        String newLevelPrefix = after.getLevel();
        String oldLevelPrefix = before.getLevel();
        if(!after.getLevel().equals(before.getLevel())){
            //获取之前部门的层次树
            List<SysDept> deptList = sysDeptMapper.getChildDeptListByLevel(before.getLevel());
            if(CollectionUtils.isEmpty(deptList)){
                for(SysDept dept : deptList){
                    String level = dept.getLevel();
                    if(level.indexOf(oldLevelPrefix) == 0){
                        //把原来部门层次下的level改为新的level
                        level = newLevelPrefix + level.substring(oldLevelPrefix.length());
                        dept.setLevel(level);
                    }
                }
                sysDeptMapper.batchUpdateLevel(deptList);
            }
        }
        sysDeptMapper.updateByPrimaryKey(after);
    }

    //判断部门是否在同一层级下
    public boolean checkExist(Integer parentId, String deptName, Integer deptId){
        return sysDeptMapper.countByNameAndParentId(parentId,deptName,deptId) > 0 ;
    }

    public String getLevel(Integer deptId){
        SysDept dept = sysDeptMapper.selectByPrimaryKey(deptId);
        if(dept == null){
            return null;
        }
        return dept.getLevel();
    }

    public void delete(int deptId){
        SysDept dept = sysDeptMapper.selectByPrimaryKey(deptId);
        Preconditions.checkNotNull(dept,"待删除的部门不存在，无法删除");
        if(sysDeptMapper.countByParentId(dept.getId()) > 0){
            throw new ParamException("当前部门下面有子部门，无法删除");
        }
        if(sysUserMapper.countByDeptId(dept.getId()) > 0){
            throw new ParamException("当前部门下面有用户，无法删除");
        }
        sysDeptMapper.deleteByPrimaryKey(deptId);
    }
}
