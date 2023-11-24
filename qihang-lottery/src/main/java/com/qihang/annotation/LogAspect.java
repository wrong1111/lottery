package com.qihang.annotation;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.support.spring.PropertyPreFilters;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qihang.common.util.area.AreaUtil;
import com.qihang.domain.log.LogDO;
import com.qihang.domain.user.SysUserDO;
import com.qihang.domain.user.UserDO;
import com.qihang.mapper.log.LogMapper;
import com.qihang.mapper.user.SysUserMapper;
import com.qihang.mapper.user.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.NamedThreadLocal;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * 操作日志记录处理
 *
 * @author ruoyi
 */
@Aspect
@Component
@Slf4j
public class LogAspect {
    @Resource
    private LogMapper logMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private AreaUtil areaUtil;

    @Resource
    SysUserMapper sysUserMapper;
    /**
     * 排除敏感属性字段
     */
    public static final String[] EXCLUDE_PROPERTIES = {"password", "oldPassword", "newPassword", "confirmPassword"};

    /**
     * 计算操作消耗时间
     */
    private static final ThreadLocal<Long> TIME_THREADLOCAL = new NamedThreadLocal<Long>("Cost Time");

    /**
     * 处理请求前执行
     */
    @Before(value = "@annotation(controllerLog)")
    public void boBefore(JoinPoint joinPoint, Log controllerLog) {
        TIME_THREADLOCAL.set(System.currentTimeMillis());
    }

    /**
     * 处理完请求后执行
     *
     * @param joinPoint 切点
     */
    @AfterReturning(pointcut = "@annotation(controllerLog)")
    public void doAfterReturning(JoinPoint joinPoint, Log controllerLog) {
        handleLog(joinPoint, controllerLog, null);
    }

    /**
     * 拦截异常操作
     *
     * @param joinPoint 切点
     * @param e         异常
     */
    @AfterThrowing(value = "@annotation(controllerLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Log controllerLog, Exception e) {
        handleLog(joinPoint, controllerLog, e);
    }

    protected void handleLog(final JoinPoint joinPoint, Log controllerLog, final Exception e) {
        try {
            // 获取当前的用户
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            String tenantO = servletRequestAttributes.getRequest().getHeader("x-tenant-id");
            String admin = servletRequestAttributes.getRequest().getHeader("x-user");//后端的
            LogDO log = new LogDO();
            Integer tenantId = null;
            if (StringUtils.isNotBlank(admin)) {
                SysUserDO user = sysUserMapper.selectOne(new QueryWrapper<SysUserDO>().lambda().eq(SysUserDO::getUsername, admin));
                log.setNickname(admin);
                log.setPhone(admin);
                log.setTypes(1);
                log.setUserId(user.getId());
                tenantId = user.getTenantId();
            } else {
                Integer userId = Integer.valueOf(servletRequestAttributes.getRequest().getAttribute("User-ID").toString());
                UserDO user = userMapper.selectById(userId);
                log.setNickname(user.getNickname());
                log.setPhone(user.getPhone());
                log.setUserId(userId);
                log.setTypes(0);
                tenantId = Integer.valueOf(tenantO);
            }
            log.setTenantId(tenantId);
            log.setCreateTime(new Date());
            String ip = areaUtil.getIp();
            log.setIp(ip);
            log.setArea(areaUtil.getAreaByIp(ip));
            log.setUpdateTime(new Date());
            // *========数据库日志=========*//
            // 请求的地址
            String uri = StringUtils.substring(servletRequestAttributes.getRequest().getRequestURI(), 0, 255);
            String param = getRequestValue(servletRequestAttributes.getRequest(), joinPoint, EXCLUDE_PROPERTIES);
            // 设置消耗时间
            long time = System.currentTimeMillis() - TIME_THREADLOCAL.get();
            String desc = controllerLog.title() + " | " + uri + " | " + time + " | " + param;
            log.setDescriptor(desc);
            // 保存数据库
            logMapper.insert(log);
        } catch (Exception exp) {
            // 记录本地异常日志
            log.error("异常信息:{}", exp.getMessage());
            exp.printStackTrace();
        } finally {
            TIME_THREADLOCAL.remove();
        }
    }


    /**
     * 获取请求的参数，放到log中
     *
     * @param
     * @throws Exception 异常
     */
    private String getRequestValue(HttpServletRequest request, JoinPoint joinPoint, String[] excludeParamNames) throws Exception {
        Map<String, String[]> map = request.getParameterMap();
        if (ObjectUtil.isNotEmpty(map)) {
            String params = JSONObject.toJSONString(map, excludePropertyPreFilter(excludeParamNames));
            return StringUtils.substring(params, 0, 2000);
        } else {
            Object args = joinPoint.getArgs();
            if (ObjectUtil.isNotNull(args)) {
                String params = argsArrayToString(joinPoint.getArgs(), excludeParamNames);
                return StringUtils.substring(params, 0, 2000);
            }
        }
        return "";
    }

    /**
     * 忽略敏感属性
     */
    public PropertyPreFilters.MySimplePropertyPreFilter excludePropertyPreFilter(String[] excludeParamNames) {
        return new PropertyPreFilters().addFilter().addExcludes(ArrayUtils.addAll(EXCLUDE_PROPERTIES, excludeParamNames));
    }

    /**
     * 参数拼装
     */
    private String argsArrayToString(Object[] paramsArray, String[] excludeParamNames) {
        String params = "";
        if (paramsArray != null && paramsArray.length > 0) {
            for (Object o : paramsArray) {
                if (ObjectUtil.isNotNull(o) && !isFilterObject(o)) {
                    try {
                        Object jsonObj = JSONObject.toJSONString(o, excludePropertyPreFilter(excludeParamNames));
                        params += jsonObj.toString() + " ";
                    } catch (Exception e) {
                    }
                }
            }
        }
        return params.trim();
    }

    /**
     * 判断是否需要过滤的对象。
     *
     * @param o 对象信息。
     * @return 如果是需要过滤的对象，则返回true；否则返回false。
     */
    @SuppressWarnings("rawtypes")
    public boolean isFilterObject(final Object o) {
        Class<?> clazz = o.getClass();
        if (clazz.isArray()) {
            return clazz.getComponentType().isAssignableFrom(MultipartFile.class);
        } else if (Collection.class.isAssignableFrom(clazz)) {
            Collection collection = (Collection) o;
            for (Object value : collection) {
                return value instanceof MultipartFile;
            }
        } else if (Map.class.isAssignableFrom(clazz)) {
            Map map = (Map) o;
            for (Object value : map.entrySet()) {
                Map.Entry entry = (Map.Entry) value;
                return entry.getValue() instanceof MultipartFile;
            }
        }
        return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof HttpServletResponse
                || o instanceof BindingResult;
    }
}
