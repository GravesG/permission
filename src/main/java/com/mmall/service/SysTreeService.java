package com.mmall.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.mmall.dao.SysAclModuleMapper;
import com.mmall.dao.SysDeptMapper;
import com.mmall.dto.AclModuleLevelDto;
import com.mmall.dto.DeptLevelDto;
import com.mmall.model.SysAclModule;
import com.mmall.model.SysDept;
import com.mmall.util.LevelUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MultiMap;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.sun.tools.doclint.Entity.ge;

@Service
public class SysTreeService {
    @Resource
    private SysDeptMapper sysDeptMapper;
    @Resource
    private SysAclModuleMapper sysAclModuleMapper;

    public List<AclModuleLevelDto> aclModuleTree(){
        List<SysAclModule> aclModuleList = sysAclModuleMapper.getAllAclModule();
        List<AclModuleLevelDto> dtoList = Lists.newArrayList();
        for(SysAclModule aclModule : aclModuleList){
            dtoList.add(AclModuleLevelDto.adapt(aclModule));
        }
        return aclModuleListToTree(dtoList);
    }

    public List<AclModuleLevelDto> aclModuleListToTree(List<AclModuleLevelDto> doList){
        if(CollectionUtils.isEmpty(doList)){
            return Lists.newArrayList();
        }
        //level -> [aclmodule1,aclmodule2,....]
        Multimap<String, AclModuleLevelDto> levelAclModuleMap = ArrayListMultimap.create();
        List<AclModuleLevelDto> rootList = Lists.newArrayList();

        for (AclModuleLevelDto dto : doList) {
            levelAclModuleMap .put(dto.getLevel(),dto);
            //如果是根目录则直接添加
            if(LevelUtil.ROOT.equals(dto.getLevel())){
                rootList.add(dto);
            }
        }

        Collections.sort(rootList,aclModuleSeqComparator);
        transformAclModuleTree(rootList,LevelUtil.ROOT,levelAclModuleMap);
        return rootList;
    }

    public void transformAclModuleTree(List<AclModuleLevelDto> doList,String level,Multimap<String, AclModuleLevelDto> levelAclModuleMap){
        for (int i = 0; i<doList.size(); i++){
            AclModuleLevelDto dto = doList.get(i);
            String nextLevel = LevelUtil.calculateLevel(level,dto.getId());
            List<AclModuleLevelDto> tempList = (List<AclModuleLevelDto>) levelAclModuleMap.get(nextLevel);
            if(CollectionUtils.isNotEmpty(tempList)){
                Collections.sort(tempList,aclModuleSeqComparator);
                dto.setAclModuleList(tempList);
                transformAclModuleTree(tempList,nextLevel,levelAclModuleMap);
            }
        }
    }

    /**
     * 取出基本数据，对数据进行封装
     * @return
     */
    public List<DeptLevelDto> deptTree(){
        //获取所有的部门
        List<SysDept> deptList = sysDeptMapper.getAllDept();

        List<DeptLevelDto> dtoList = Lists.newArrayList();
        for (SysDept dept: deptList) {
            DeptLevelDto dto = DeptLevelDto.adapt(dept);
            dtoList.add(dto);
        }
        return deptListToTree(dtoList);
    }


    public List<DeptLevelDto> deptListToTree(List<DeptLevelDto> deptLevelList){
        if(CollectionUtils.isEmpty(deptLevelList)){
            return  Lists.newArrayList();
        }
        //level -> [dept1,dept2,....]
        Multimap<String, DeptLevelDto> levelDeptMap = ArrayListMultimap.create();

        List<DeptLevelDto> rootList = Lists.newArrayList();

        for (DeptLevelDto dto : deptLevelList) {
            levelDeptMap.put(dto.getLevel(),dto);
            //如果是根目录则直接添加
            if(LevelUtil.ROOT.equals(dto.getLevel())){
                rootList.add(dto);
            }
        }

        //把rootList根据seq从小到大排序
        Collections.sort(rootList, new Comparator<DeptLevelDto>() {
            @Override
            public int compare(DeptLevelDto o1, DeptLevelDto o2) {
                return o1.getSeq() - o2.getSeq();
            }
        });

        //对当前层做树形结构的处理
        transformDeptTree(deptLevelList,LevelUtil.ROOT,levelDeptMap);
        return rootList;
    }


    public void transformDeptTree(List<DeptLevelDto> deptLevelList, String level, Multimap<String, DeptLevelDto> levelDeptMap){
        for (int i = 0; i < deptLevelList.size() ; i++) {
            //遍历该层的每个元素
            DeptLevelDto deptLevelDto = deptLevelList.get(i);
            //处理当前层级的数据
            String nextLevel = LevelUtil.calculateLevel(level,deptLevelDto.getId());
            //处理下一层
            List<DeptLevelDto> tempDeptList = (List<DeptLevelDto>)levelDeptMap.get(nextLevel);

            if(!CollectionUtils.isEmpty(tempDeptList)){
                //排序
                Collections.sort(tempDeptList,deptSeqComparator);
                //设置下一层部门
                deptLevelDto.setDeptList(tempDeptList);
                //进去到下一层管理
                transformDeptTree(tempDeptList,nextLevel,levelDeptMap);
            }
        }
    }

    public Comparator<DeptLevelDto> deptSeqComparator = new Comparator<DeptLevelDto>() {
        @Override
        public int compare(DeptLevelDto o1, DeptLevelDto o2) {
            return o1.getSeq() - o2.getSeq();
        }
    };

    public Comparator<AclModuleLevelDto> aclModuleSeqComparator = new Comparator<AclModuleLevelDto>() {
        @Override
        public int compare(AclModuleLevelDto o1, AclModuleLevelDto o2) {
            return o1.getSeq() - o2.getSeq();
        }
    };
}
