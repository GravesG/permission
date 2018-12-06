package com.mmall.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mmall.exception.ParamException;
import org.apache.commons.collections.MapUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.*;

public class BeanValidator {
    //定义全局的校验工厂
    private static ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();

    //定义校验方法
    /**
     *单个字段的校验
     * @param t
     * @param groups
     * @param <T>
     * @return 返回值map 前面的String是字段名称，后一个String是错误信息
     */
    public static <T> Map<String,String> validate(T t, Class... groups){
        //从工厂获取validator
        Validator validator = validatorFactory.getValidator();
        //自动获取校验结果
        Set validateResult = validator.validate(t,groups);
        if(validateResult.isEmpty()){
            return Collections.EMPTY_MAP;
        }else{
            LinkedHashMap errors = Maps.newLinkedHashMap();
            //遍历validateResult
            Iterator iterator = validateResult.iterator();
            while (iterator.hasNext()){
                //获取违反约束的对象
                ConstraintViolation violation = (ConstraintViolation)iterator.next();
                errors.put(violation.getPropertyPath(),violation.getMessage());
            }
            return errors;
        }
    }

    /**
     * 对多个字段进行校验
     * @param collection
     * @return
     */
    public static Map<String,String> validateList(Collection<?> collection){
        //非空判断  google提供的
        Preconditions.checkNotNull(collection);
        Iterator iterator = collection.iterator();
        Map errors;
        do{
            if(!iterator.hasNext()){
                return Collections.emptyMap();
            }
            Object object = iterator.next();
            errors = validate(object,new Class[0]);
        }while (errors.isEmpty());
        return errors;
    }

    /**
     * 对单个参数和多个参数的方法进行封装
     * @param first
     * @param objects
     * @return
     */
    public static Map<String,String> validateObject(Object first,Object...objects){
        if(objects != null && objects.length>0){
            return validateList(Lists.asList(first,objects));
        }else{
            return validate(first,new Class[0]);
        }
    }

    public static void check(Object param) throws ParamException{
        Map<String,String> map = BeanValidator.validateObject(param);
        if(MapUtils.isNotEmpty(map)){
            throw new ParamException(map.toString());
        }
    }
}
